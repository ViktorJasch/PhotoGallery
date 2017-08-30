package com.example.photogallery.mvp.map;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.photogallery.Constants;
import com.example.photogallery.PicassoHelper;
import com.example.photogallery.mvp.model.GeoGalleryItem;
import com.example.photogallery.mvp.model.GeoPhotosInfo;
import com.example.photogallery.mvp.model.Photos;
import com.example.photogallery.mvp.model.network.RequestsManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;

import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by viktor on 05.07.17.
 */

public class LocatrPresenter extends MvpBasePresenter<LocatrView> {
    private static final String TAG = "LocatrPresenter";
    private GoogleApiClient mClient;
    private Location mCurrentLocation;
    @Inject RequestsManager mRequestsManager;
    @Inject PicassoHelper mPicassoHelper;

    @Inject
    protected LocatrPresenter(){

    }

    protected void findImage(){

        Log.d(TAG, "findImage: view is " + isViewAttached());
        final LocationRequest locReq = LocationRequest.create();
        locReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locReq.setNumUpdates(1);
        locReq.setInterval(0);
        getView().showLoading(false);

        try{
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mClient, locReq,
                            (location -> doLocationChanged(location)));
        } catch (SecurityException exc){
            Log.i(TAG, "findImage: SecurityException is caught: " + exc);
        }
    }

    protected void updateLocation(int margin){
        LatLng myPoint = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(myPoint, 10.0f);
        MarkerOptions myMarker = new MarkerOptions()
                .position(myPoint);
        getView().showPhoto(update, myMarker);
    }

    protected void onMapViewCreate(Context context){
        googleApiClientInit(context);
    }

    protected void onStart(){
        mClient.connect();
    }

    protected void onStop(){
        mClient.disconnect();
    }

    protected boolean isGoogleApiClientConnected(){
        return mClient.isConnected();
    }

    private void googleApiClientInit(Context context) {
        mClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(TAG, "onConnected: ");
                        getView().invalidateMenu();
                        findImage();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .build();
    }

    private void searchGeoPhoto(String lat, String lon){
        mRequestsManager.searchGeoPhoto(lat, lon).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(geoPhotosInfoPhotos -> doNext(geoPhotosInfoPhotos),
                        throwable -> doError(throwable),
                        () -> doCompleted());
    }

    private void doNext(Photos<GeoPhotosInfo> geoPhotosInfoPhotos){
        GeoPhotosInfo gpi = geoPhotosInfoPhotos.getInfo();
        //ограничение загружаемых фото
        List<GeoGalleryItem> list = gpi.getPhoto().subList(0, Constants.COUNT_LOCATR_PHOTO_LOADING);
        mPicassoHelper.cechingPhotos(list);
        getView().setData(list);

    }

    private void doError(Throwable e){
        getView().showError(e, false);
    }

    private void doCompleted(){
        getView().showContent();
    }

    private void doLocationChanged(Location location){
        Log.i(TAG, "Got a fix: " + location);
        mCurrentLocation = location;
        searchGeoPhoto("" + location.getLatitude(),"" + location.getLongitude());
    }
}
