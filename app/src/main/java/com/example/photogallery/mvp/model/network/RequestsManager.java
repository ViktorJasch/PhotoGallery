package com.example.photogallery.mvp.model.network;

import com.example.photogallery.Constants;
import com.example.photogallery.mvp.model.GeoPhotosInfo;
import com.example.photogallery.mvp.model.Photos;
import com.example.photogallery.mvp.model.PhotosInfo;

import rx.Observable;

/**
 * Created by viktor on 03.07.17.
 */

public class RequestsManager {
    private static final String TAG = "RequestsManager";
    private FlickrApi mService;

    public RequestsManager(FlickrApi service){
        mService = service;
    }

    public Observable<Photos<PhotosInfo>> getRecentPhoto(final int page){
       return mService.getRecentPhotos(Constants.API_KEY, "url_s", page, "json", "1");
    }

    public Observable<Photos<PhotosInfo>> searchPhoto(String query, final int page){
        return mService.searchPhoto(Constants.API_KEY, "url_s", page, query, "json", "1");
    }

    public Observable<Photos<GeoPhotosInfo>> searchGeoPhoto(String lat, String lon){
        return mService.searchGeoPhoto(Constants.API_KEY, "url_s,geo", lat, lon, "json", "1");
    }
}
