package com.example.photogallery.photos;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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

import com.example.photogallery.model.GalleryItem;
import com.example.photogallery.PhotoPageActivity;
import com.example.photogallery.PollService;
import com.example.photogallery.QueryPreferences;
import com.example.photogallery.R;
import com.example.photogallery.ThumbnailDownload;
import com.example.photogallery.bus.PhotoHolderClickedEvent;
import com.hannesdorfmann.mosby3.mvp.lce.MvpLceFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Виктор on 07.12.2016.
 */

public class PhotoGalleryFragment extends
        MvpLceFragment<SwipeRefreshLayout, List<GalleryItem>,
        PhotosView, PhotosPresenter> implements PhotosView{
    private static final String TAG = "PhotoGalleryFragment";

    @BindView(R.id.fragment_photoGallery_recycleView)
    RecyclerView mRecyclerView;
    @BindDrawable(R.drawable.bill_up_close)
    Drawable defaultDrawable;

    List<GalleryItem> mList = new ArrayList<>();
    private PhotoAdapter adapter;
    private ThumbnailDownload<PhotoAdapter.PhotoHolder> mThumbnailDownload;
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
        mOnShowNotification = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "onReceive: canceling notification");
                setResultCode(Activity.RESULT_CANCELED);
            }
        };
        handlerThreadPrepare();
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
        mThumbnailDownload.quit();
        Log.i(TAG, "onDestroy: Background thread destroyed");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //при нажатии на элемент представления, будет получено фото в полный объем,
        // но при повороте экрана может оказаться, что ThumbnailDownload
        // (которому плевать на изменение конфигурации), не связан с текущим представлением
        mThumbnailDownload.clearQueue();
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
        adapter = new PhotoAdapter(mThumbnailDownload, defaultDrawable);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        setupAdapter();
    }

    private void handlerThreadPrepare(){
        Handler responseHandler = new Handler();
        mThumbnailDownload = new ThumbnailDownload<>(responseHandler);
        mThumbnailDownload.setThumbDownloadListener(new ThumbnailDownload.ThumbnailDownloadListener<PhotoAdapter.PhotoHolder>(){
            @Override
            public void onThumbDownloaded(PhotoAdapter.PhotoHolder obj, Bitmap thumbnail) {
                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                obj.bindImage(drawable);
            }
        });
        mThumbnailDownload.start();
        mThumbnailDownload.getLooper();
    }

    @Override
    public void showContent() {
        super.showContent();
        contentView.setRefreshing(false);
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
        adapter.setPhotos(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void loadData(boolean pullToRefresh) {
        String query = QueryPreferences.getStoredQuery(getActivity());
        Log.d(TAG, "loadData: presenter == null: " + (presenter == null));
        presenter.updateItems(query);
    }

    @Subscribe
    public void onPhotoHolderClickedEvent(PhotoHolderClickedEvent event){
        Intent i = PhotoPageActivity
                .newIntent(getActivity(), event.getPhotoUri());
        startActivity(i);
    }
}