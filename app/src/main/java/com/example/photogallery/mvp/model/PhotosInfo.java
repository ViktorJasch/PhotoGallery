package com.example.photogallery.mvp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by viktor on 03.07.17.
 */

public class PhotosInfo extends AbstractPhotosInfo<GalleryItem>{
    @SerializedName("photo")
    private List<GalleryItem> photo;

    public List<GalleryItem> getPhoto() {
        return photo;
    }

    public void setPhoto(List<GalleryItem> photo) {
        this.photo = photo;
    }
}
