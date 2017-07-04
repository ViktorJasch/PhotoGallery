package com.example.photogallery.photos;

import android.os.AsyncTask;
import android.util.Log;

import com.example.photogallery.Constants;
import com.example.photogallery.model.Photos;
import com.example.photogallery.model.PhotosInfo;
import com.example.photogallery.network.FlickrApi;
import com.example.photogallery.network.FlickrFetch;
import com.example.photogallery.model.GalleryItem;
import com.example.photogallery.network.RetrofitClient;
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by viktor on 02.07.17.
 */

public class PhotosPresenter extends MvpBasePresenter<PhotosView> {
    private static final String TAG = "PhotosPresenter";
    FlickrApi client = RetrofitClient
            .getService(FlickrApi.class);

    public void updateItems(String query){
        getView().showLoading(false);
//        new FetchItemsTask(query).execute();
        if(query == null)
            getRecentPhoto();
        else
            searchPhoto(query);
    }

    /** Решение с AsyncTask */
    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {
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
            getView().setData(items);
            getView().showContent();
//            mList = items;
//            setupAdapter();
        }
    }

    private void getRecentPhoto(){
        Log.d(TAG, "getRecentPhoto: called");
        client.getRecentPhotos(Constants.API_KEY, "url_s", "json", "1").enqueue(new Callback<Photos>() {
            @Override
            public void onResponse(Call<Photos> call, Response<Photos> response) {
                Photos photos = response.body();
                List<GalleryItem> items = photos
                        .getInfo()
                        .getPhoto();
                getView().setData(items);
                getView().showContent();
            }

            @Override
            public void onFailure(Call<Photos> call, Throwable t) {
                Log.d(TAG, "onFailure: ");
                getView().showError(t, false);
            }
        });
    }

    private void searchPhoto(String query){
        client.searchPhoto(Constants.API_KEY, "url_s", query, "json", "1").enqueue(new Callback<Photos>() {
            @Override
            public void onResponse(Call<Photos> call, Response<Photos> response) {
                Photos photos = response.body();
                List<GalleryItem> items = photos
                        .getInfo()
                        .getPhoto();
                getView().setData(items);
                getView().showContent();
            }

            @Override
            public void onFailure(Call<Photos> call, Throwable t) {
                getView().showError(t, false);
            }
        });
    }
}
