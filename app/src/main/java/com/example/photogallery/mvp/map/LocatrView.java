package com.example.photogallery.mvp.map;

import android.location.Location;

import com.example.photogallery.mvp.model.GeoGalleryItem;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hannesdorfmann.mosby3.mvp.lce.MvpLceView;

import java.util.List;

/**
 * Created by viktor on 05.07.17.
 */

public interface LocatrView extends MvpLceView<List<GeoGalleryItem>> {
    void showPhoto(CameraUpdate update, MarkerOptions myMarker);
}
