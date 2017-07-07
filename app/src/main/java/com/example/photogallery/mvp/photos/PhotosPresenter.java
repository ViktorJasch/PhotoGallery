package com.example.photogallery.mvp.photos;

import android.os.AsyncTask;
import android.util.Log;

import com.example.photogallery.Constants;
import com.example.photogallery.mvp.model.Photos;
import com.example.photogallery.mvp.model.PhotosInfo;
import com.example.photogallery.network.FlickrApi;
import com.example.photogallery.mvp.model.GalleryItem;
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
    private int searchPage = 1;
    private int recentPage = 1;
    FlickrApi client = RetrofitClient
            .getService(FlickrApi.class);

    public void loadMore(String query){
        getView().showLoading(false);
        if(query == null){
            getRecentPhoto(recentPage);
            recentPage++;
        }
        else{
            searchPhoto(query, searchPage);
            searchPage++;
        }
    }

    public void updateItems(String query, boolean pullToRefresh){
        getView().showLoading(pullToRefresh);
        if(query == null)
            getRecentPhoto(1);
        else
            searchPhoto(query, 1);
    }

    private void getRecentPhoto(int page){
        Log.d(TAG, "getRecentPhoto: called");
        client.getRecentPhotos(Constants.API_KEY, "url_s", page, "json", "1").enqueue(new Callback<Photos<PhotosInfo>>() {
            @Override
            public void onResponse(Call<Photos<PhotosInfo>> call, Response<Photos<PhotosInfo>> response) {
                Photos photos = response.body();
                PhotosInfo photosInfo = (PhotosInfo) photos.getInfo();
                List<GalleryItem> items = photosInfo
                        .getPhoto();
                getView().setData(items);
                getView().showContent();
            }

            @Override
            public void onFailure(Call<Photos<PhotosInfo>> call, Throwable t) {
                Log.d(TAG, "onFailure: ");
                getView().showError(t, false);
            }
        });
    }

    private void searchPhoto(String query, int page){
        client.searchPhoto(Constants.API_KEY, "url_s", page, query, "json", "1").enqueue(new Callback<Photos<PhotosInfo>>() {
            @Override
            public void onResponse(Call<Photos<PhotosInfo>> call, Response<Photos<PhotosInfo>> response) {
                Photos photos = response.body();
                List<GalleryItem> items = photos
                        .getInfo()
                        .getPhoto();
                getView().setData(items);
                getView().showContent();
            }

            @Override
            public void onFailure(Call<Photos<PhotosInfo>> call, Throwable t) {
                getView().showError(t, false);
            }
        });
    }
}
