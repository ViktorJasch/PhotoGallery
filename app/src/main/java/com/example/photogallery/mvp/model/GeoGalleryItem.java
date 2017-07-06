package com.example.photogallery.mvp.model;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by viktor on 04.07.17.
 */

public class GeoGalleryItem extends GalleryItem implements ClusterItem{
    @SerializedName("latitude")
    private double mLat;
    @SerializedName("longitude")
    private double mLng;

    public double getLat() {
        return mLat;
    }

    public double getLng() {
        return mLng;
    }

    public void setLat(double mLat) {
        this.mLat = mLat;
    }

    public void setLng(double mLng) {
        this.mLng = mLng;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(mLat, mLng);
    }
}
