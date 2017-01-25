package com.example.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Виктор on 07.12.2016.
 */

public class PhotoGalleryFragment extends VisibleFragment {
    private static final String TAG = "PhotoGalleryFragment";
    private RecyclerView mRecyclerView;
    List<GalleryItem> mList = new ArrayList<>();
    private ThumbnailDownload<PhotoHolder> mThumbnailDownload;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        updateItems();

        Handler responseHandler = new Handler();
        mThumbnailDownload = new ThumbnailDownload<>(responseHandler);
        mThumbnailDownload.setThumbDownloadListener(new ThumbnailDownload.ThumbnailDownloadListener<PhotoHolder>(){
            @Override
            public void onThumbDownloaded(PhotoHolder obj, Bitmap thumbnail) {
                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                obj.bindHolder(drawable);
            }
        });
        mThumbnailDownload.start();
        mThumbnailDownload.getLooper();
        Log.i(TAG, "onCreate: Background thread started");
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mRecyclerView = (RecyclerView)view.findViewById(R.id.fragment_photoGallery_recycleView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        setupAdapter();

        return view;
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
                updateItems();
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
                updateItems();
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

    /**Необходимости для RecycleView*/
    private class PhotoHolder extends RecyclerView.ViewHolder{
        ImageView mPhoto;

        public PhotoHolder(View itemView) {
            super(itemView);

            mPhoto = (ImageView) itemView;
        }

        public void bindHolder(Drawable item){
            mPhoto.setImageDrawable(item);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{
        List<GalleryItem> mGalleryItems = new ArrayList<>();

        public PhotoAdapter(List<GalleryItem> items){
            mGalleryItems = items;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = mList.get(position);
            Drawable defaultPhoto = getResources().getDrawable(R.drawable.bill_up_close);
            holder.bindHolder(defaultPhoto);

            mThumbnailDownload.queueThumbnail(holder, galleryItem.getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }
    /**C RecycleView закончили*/

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>>{
        private String mQuery;

        public FetchItemsTask(String query){
            mQuery = query;
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            if(mQuery == null)
                return new FlickrFetch().fetchRecentPhoto();
            else
                return new FlickrFetch().searchPhoto(mQuery);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            mList = items;
            setupAdapter();
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
            mRecyclerView.setAdapter(new PhotoAdapter(mList));
        }
    }

    private void updateItems(){
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query).execute();
    }
}
