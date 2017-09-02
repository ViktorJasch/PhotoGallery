package com.example.photogallery.mvp.photos;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.photogallery.app.PhotoGalleryApp;
import com.example.photogallery.mvp.model.GalleryItem;
import com.example.photogallery.mvp.web_page.*;
import com.example.photogallery.PhotoAdapter;
import com.example.photogallery.R;
import com.example.photogallery.bus.PhotoHolderClickedEvent;
import com.example.photogallery.mvp.photos.di.PhotosComponent;
import com.example.photogallery.mvp.photos.di.PhotosModule;
import com.example.photogallery.permissions.BasePermissionDefinition;
import com.example.photogallery.service.PollService;
import com.hannesdorfmann.mosby3.mvp.lce.MvpLceFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Виктор on 07.12.2016.
 */

public class PhotoGalleryFragment extends
        MvpLceFragment<SwipeRefreshLayout, List<GalleryItem>,
        PhotosView, PhotosPresenter> implements PhotosView,
        SwipeRefreshLayout.OnRefreshListener, CustomScrollListener.LoadingListener{
    private static final String TAG = "PhotoGalleryFragment";

    @BindView(R.id.fragment_photoGallery_recycleView)
    RecyclerView mRecyclerView;
    @BindDrawable(R.drawable.bill_up_close)
    Drawable defaultDrawable;


    @Inject PhotoAdapter adapter;
    private GridLayoutManager manager;
    private CustomScrollListener scrollListener;
    private ProgressDialog mProgressDialog;
    //для пагинации списка
    private boolean mLoadingMoreElements = false;
    //Данный приемник срабатывает только когда приложение запущенно (так как он динамический).
    //Фильтром является ACTION_SHOW_NOTIFICATION. Сообщения с таким тегом вызываются при появлении новых фото
    //в классе PollService методом showBackgroundNotification.
    //Если этот применик существует (т.е. приложение запущенно), уведомление отменяется (что б не бесило),
    private BroadcastReceiver mOnShowNotification;
    private PhotosComponent mPhotosComponent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPhotosComponent();
        mPhotosComponent.inject(this);
        Log.d(TAG, "onCreate: ");
        setHasOptionsMenu(true);
        setRetainInstance(true);
        mOnShowNotification = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "onReceive: canceling notification");
                setResultCode(Activity.RESULT_CANCELED);
            }
        };
        Log.i(TAG, "onCreate: Background thread started");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        ButterKnife.bind(this, view);
        recycleViewPrepare();
        Log.d(TAG, "onCreateView: view = null: " + (view == null));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        contentView.setOnRefreshListener(this);
        loadData(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        getActivity().unregisterReceiver(mOnShowNotification);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                presenter.onQuerySet(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setQuery(presenter.getQueryString(), false);
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if(PollService.isServiceAlarmOn(getActivity()))
            toggleItem.setTitle(R.string.stop_polling);
        else
            toggleItem.setTitle(R.string.start_polling);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_clear:
                presenter.onClearItemClick();
                return true;
            case R.id.menu_item_toggle_polling:
                presenter.onTogglePollingItemClick(getActivity());
                getActivity().invalidateOptionsMenu();
                return true;
            case R.id.action_locate:
                presenter.onActionLocateItemClick(getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Метод инициализирует при необходимости адаптер если выполняется условие. Условием является
     * привязанность фрагмента к активности. В данном приложении используется удержание фрагмента.
     * Метод вызывается в onPostExecute.
     * Это означает, что возможен такой вариант, что при смене конфигурации активность уничтожится,
     * а в теле фрагмента закончит выполняться фоновый поток и вызовется метод onPostExecute.
     * По идее, должен обновиться mRecycleView, но этот элемент не будет привязан к активности (она ведь уничтожена),
     * а значит не будет привязан и к представлению (view) и обновление вызовет ошибку.
     * Поэтому необходимо проверять (isAdded) привязан ли фрагмент к активности
     */
    private void setupAdapter(){
        if(isAdded()){
            mRecyclerView.setAdapter(adapter);
        }
    }

    private void recycleViewPrepare(){
        manager = new GridLayoutManager(getActivity(), 3);
        scrollListener = new CustomScrollListener(manager, this);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addOnScrollListener(scrollListener);
        setupAdapter();
    }

    @Override
    protected String getErrorMessage(Throwable e, boolean pullToRefresh) {
        return null;
    }
    
    @Nullable
    @Override
    public PhotosPresenter createPresenter() {
        return mPhotosComponent.presenter();
    }

    @Override
    public void setData(List<GalleryItem> data) {
        setLoadingMoreElements(false);
        adapter.setPhotos(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void loadData(boolean pullToRefresh) {
        presenter.loadItems(pullToRefresh);
    }

    @Override
    public void showLoading(boolean pullToRefresh) {
        if(mLoadingMoreElements){
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.show();
        } else
            super.showLoading(pullToRefresh);
    }

    @Override
    public void showContent() {
        if(mProgressDialog != null){
            mProgressDialog.cancel();
            mProgressDialog = null;
        }
        contentView.setRefreshing(false);
        super.showContent();
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "onRefresh: ");
        contentView.setRefreshing(true);
        loadData(true);
    }

    @Override
    public void needMoreElements() {
        Log.d(TAG, "needMoreElements: called");
        setLoadingMoreElements(true);
        presenter.loadMore();
    }

    @Override
    public void requestAndExplainPermission(BasePermissionDefinition permissionDefinition) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                permissionDefinition.getPermission())) {
            Log.d(TAG, "locationPermissionRequestAndExplain: need explain");
            createPermissionAlertDialog(permissionDefinition).show();
        } else {
            Log.d(TAG, "locationPermissionRequestAndExplain: request");
            requestPermission(permissionDefinition);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: start, request code = " + requestCode);
        presenter.onRequestPermissionResult(requestCode, grantResults, getActivity());
    }

    private void setLoadingMoreElements(boolean isLoading){
        mLoadingMoreElements = isLoading;
        scrollListener.setLoading(isLoading);
    }

    private AlertDialog createPermissionAlertDialog(BasePermissionDefinition permissionDefinition){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(permissionDefinition.getDescription());
        builder.setPositiveButton(android.R.string.ok, ((dialog, which) -> requestPermission(permissionDefinition)));
        return builder.create();
    }

    private void requestPermission(BasePermissionDefinition permissionDefinition) {
        requestPermissions(
                new String[]{permissionDefinition.getPermission()},
                permissionDefinition.getRequestCode());
    }

    private void initPhotosComponent(){
        mPhotosComponent = PhotoGalleryApp.get(getActivity()).getAppComponent()
                .plus(new PhotosModule());
    }

    @Subscribe
    public void onPhotoHolderClickedEvent(PhotoHolderClickedEvent event){
        Intent i = PhotoPageActivity
                .newIntent(getActivity(), event.getPhotoUri());
        startActivity(i);
    }
}