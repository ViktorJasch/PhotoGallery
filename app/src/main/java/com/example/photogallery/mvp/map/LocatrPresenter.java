package com.example.photogallery.mvp.map;

import android.location.Location;
import android.util.Log;

import com.example.photogallery.Constants;
import com.example.photogallery.R;
import com.example.photogallery.mvp.model.GalleryItem;
import com.example.photogallery.mvp.model.GeoGalleryItem;
import com.example.photogallery.mvp.model.GeoPhotosInfo;
import com.example.photogallery.mvp.model.Photos;
import com.example.photogallery.network.FlickrApi;
import com.example.photogallery.network.Requests;
import com.example.photogallery.network.RetrofitClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.geometry.Point;
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by viktor on 05.07.17.
 */

public class LocatrPresenter extends MvpBasePresenter<LocatrView> {
    private static final String TAG = "LocatrPresenter";
    private Location currentLocation;

    protected LocatrPresenter(LocatrView view){
        attachView(view);
    }

    protected void findImage(GoogleApiClient gac){

        Log.d(TAG, "findImage: view is " + isViewAttached());
        final LocationRequest locReq = LocationRequest.create();
        locReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locReq.setNumUpdates(1);
        locReq.setInterval(0);
        getView().showLoading(false);

        try{
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(gac, locReq, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.i(TAG, "Got a fix: " + location);
                            currentLocation = location;
                            searchGeoPhoto("" + location.getLatitude(),"" + location.getLongitude());
                        }
                    });
        } catch (SecurityException exc){
            Log.i(TAG, "findImage: SecurityException is caught: " + exc);
        }
    }

    protected void updateLocation(int margin){
        LatLng myPoint = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(myPoint, 10.0f);
        MarkerOptions myMarker = new MarkerOptions()
                .position(myPoint);
        getView().showPhoto(update, myMarker);
    }

    private void searchGeoPhoto(String lat, String lon){
        Requests.searchGeoPhoto(lat, lon).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Photos<GeoPhotosInfo>>() {
                    @Override
                    public void onCompleted() {
                        getView().showContent();
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().showError(e, false);
                    }

                    @Override
                    public void onNext(Photos<GeoPhotosInfo> geoPhotosInfoPhotos) {
                        GeoPhotosInfo gpi = geoPhotosInfoPhotos.getInfo();
                        List<GeoGalleryItem> list = gpi.getPhoto();
                        getView().setData(list.subList(0, 50));
                    }
                });
    }
}
