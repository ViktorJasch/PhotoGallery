package com.example.photogallery.mvp.photos;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import com.example.photogallery.CustomScrollListener;
import com.example.photogallery.mvp.map.LocatrActivity;
import com.example.photogallery.mvp.model.GalleryItem;
import com.example.photogallery.mvp.page.*;
import com.example.photogallery.PhotoAdapter;
import com.example.photogallery.QueryPreferences;
import com.example.photogallery.R;
import com.example.photogallery.bus.PhotoHolderClickedEvent;
import com.hannesdorfmann.mosby3.mvp.lce.MvpLceFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 1;

    @BindView(R.id.fragment_photoGallery_recycleView)
    RecyclerView mRecyclerView;
    @BindDrawable(R.drawable.bill_up_close)
    Drawable defaultDrawable;

    private PhotoAdapter adapter;
    private GridLayoutManager manager;
    private CustomScrollListener scrollListener;
    private List<GalleryItem> items;
    private ProgressDialog pd;
    //для бесконечной прокрутки
    boolean loadingMoreElements = false;
    //Данный приемник срабатывает только когда приложение запущенно (так как он динамический).
    //Фильтром является ACTION_SHOW_NOTIFICATION. Сообщения с таким тегом вызываются при появлении новых фото
    //в классе PollService методом showBackgroundNotification.
    //Если этот применик существует (т.е. приложение запущенно), уведомление отменяется (что б не бесило),
    private BroadcastReceiver mOnShowNotification;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setHasOptionsMenu(true);
        setRetainInstance(true);
        items = new ArrayList<>(128);
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
    public void onDestroy() {
        super.onDestroy();
        //mThumbnailDownload.quit();
        Log.i(TAG, "onDestroy: Background thread destroyed");
    }

    //Этот участок нужен был до использования Picasso
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //при нажатии на элемент представления, будет получено фото в полный объем,
        // но при повороте экрана может оказаться, что ThumbnailDownload
        // (которому плевать на изменение конфигурации), не связан с текущим представлением
        //mThumbnailDownload.clearQueue();
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
                QueryPreferences.setStoredQuery(getActivity(), query);
                loadData(false);
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
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
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
                QueryPreferences.setStoredQuery(getActivity(), null);
                loadData(false);
                return true;
            case R.id.menu_item_toggle_polling:
                boolean ShouldStartAlarm = PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), !ShouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;
            case R.id.action_locate:
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "loadData: if permissions need");
                    locationPermissionRequestAndExplain();
                } else{
                    Intent intent = LocatrActivity.newIntent(getActivity());
                    startActivity(intent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Метод инициализирует при необходимости адаптер если выполняется условие. Условием является
     * привязанность фрагмента к активности. В данном приложении используется удержание фрагмента,
     * а так же фоновый поток, реализованный с помощью AsyncTask. Метод вызывается в onPostExecute.
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
        adapter = new PhotoAdapter(getActivity());
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
        Log.d(TAG, "createPresenter: called");
        return new PhotosPresenter();
    }

    @Override
    public void setData(List<GalleryItem> data) {
        if(loadingMoreElements){
            List<GalleryItem> freshItems = data;
            Collections.reverse(freshItems);
            items.addAll(freshItems);
            setLoadingMoreElements(false);
        } else{
            items = data;
            Collections.reverse(items);
        }
        adapter.setPhotos(items);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void loadData(boolean pullToRefresh) {
        String query = QueryPreferences.getStoredQuery(getActivity());
        Log.d(TAG, "loadData: presenter == null: " + (presenter == null));
        presenter.updateItems(query, pullToRefresh);
    }

    @Override
    public void showLoading(boolean pullToRefresh) {
        if(loadingMoreElements){
            pd = new ProgressDialog(getActivity());
            pd.show();
        } else
            super.showLoading(pullToRefresh);
    }

    @Override
    public void showContent() {
        if(pd != null){
            pd.cancel();
            pd = null;
        }
        contentView.setRefreshing(false);
        super.showContent();
    }

    @Override
    public void loadMoreData() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        Log.d(TAG, "loadData: presenter == null: " + (presenter == null));
        presenter.loadMore(query);
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "onRefresh: ");
        contentView.setRefreshing(true);
        loadData(true);
    }

    @Override
    public void loadMoreItems() {
        Log.d(TAG, "loadMoreItems: called");
        setLoadingMoreElements(true);
        loadData(false);
    }

    private void setLoadingMoreElements(boolean isLoading){
        loadingMoreElements = isLoading;
        scrollListener.setLoading(isLoading);
    }

    //Методы для запроса разрешение на получение геопозиционных данных
    private void locationPermissionRequestAndExplain() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.d(TAG, "locationPermissionRequestAndExplain: need explain");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.fine_location_explanation);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestFineLocationPermission();
                }
            });
            builder.create().show();

        } else {
            Log.d(TAG, "locationPermissionRequestAndExplain: request");
            requestFineLocationPermission();
        }
    }

    private void requestFineLocationPermission() {
        requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ACCESS_FINE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: start, request code = " + requestCode);
        switch (requestCode){
            case PERMISSION_ACCESS_FINE_LOCATION:
                Log.d(TAG, "onRequestPermissionsResult: switch");
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent intent = LocatrActivity.newIntent(getActivity());
                    startActivity(intent);
                }
                return;
        }
    }

    @Subscribe
    public void onPhotoHolderClickedEvent(PhotoHolderClickedEvent event){
        Intent i = PhotoPageActivity
                .newIntent(getActivity(), event.getPhotoUri());
        startActivity(i);
    }
}