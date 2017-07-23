package com.example.photogallery.network;

import android.util.Log;

import com.example.photogallery.Constants;
import com.example.photogallery.mvp.model.GeoGalleryItem;
import com.example.photogallery.mvp.model.GeoPhotosInfo;
import com.example.photogallery.mvp.model.Photos;
import com.example.photogallery.mvp.model.PhotosInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;

/**
 * Created by viktor on 03.07.17.
 */

public class Requests {
    private static final String TAG = "Requests";
    private static FlickrApi client = RetrofitClient
            .getService(FlickrApi.class);

    public static Observable<Photos<PhotosInfo>> getRecentPhoto(final int page){
       return client.getRecentPhotos(Constants.API_KEY, "url_s", page, "json", "1");
    }

    public static Observable<Photos<PhotosInfo>> searchPhoto(String query, final int page){
        return client.searchPhoto(Constants.API_KEY, "url_s", page, query, "json", "1");
    }

    public static Observable<Photos<GeoPhotosInfo>> searchGeoPhoto(String lat, String lon){
        return client.searchGeoPhoto(Constants.API_KEY, "url_s,geo", lat, lon, "json", "1");
    }

}
