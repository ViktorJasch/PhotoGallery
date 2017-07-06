package com.example.photogallery.mvp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by viktor on 05.07.17.
 */

public class GeoPhotosInfo extends AbstractPhotosInfo<GeoGalleryItem> {
    @SerializedName("photo")
    private List<GeoGalleryItem> photo;

    @Override
    public List<GeoGalleryItem> getPhoto() {
        return photo;
    }

    @Override
    public void setPhoto(List<GeoGalleryItem> photo) {

    }
}
