package com.example.photogallery.mvp.map;

import android.location.Location;
import android.util.Log;

import com.example.photogallery.Constants;
import com.example.photogallery.mvp.model.GeoGalleryItem;
import com.example.photogallery.mvp.model.GeoPhotosInfo;
import com.example.photogallery.mvp.model.Photos;
import com.example.photogallery.network.FlickrApi;
import com.example.photogallery.network.RetrofitClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by viktor on 05.07.17.
 */

public class LocatrPresenter extends MvpBasePresenter<LocatrView> {
    private static final String TAG = "LocatrPresenter";
    private FlickrApi client = RetrofitClient
            .getService(FlickrApi.class);
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

    private void searchGeoPhoto(String lat, String lon){
        client.searchGeoPhoto(Constants.API_KEY, "url_s,geo", lat, lon, "json", "1")
                .enqueue(new Callback<Photos<GeoPhotosInfo>>() {
            @Override
            public void onResponse(Call<Photos<GeoPhotosInfo>> call, Response<Photos<GeoPhotosInfo>> response) {
                Photos<GeoPhotosInfo> photos = response.body();
                GeoPhotosInfo gpi = photos.getInfo();
                List<GeoGalleryItem> list = gpi.getPhoto();
                getView().setLocation(currentLocation);
                getView().showContent();
                getView().setData(list.subList(0, 50));
            }

            @Override
            public void onFailure(Call<Photos<GeoPhotosInfo>> call, Throwable t) {
                getView().showError(t, false);
            }
        });
    }
}
