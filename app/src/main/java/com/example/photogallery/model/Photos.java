package com.example.photogallery.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by viktor on 03.07.17.
 */

public class Photos {
    @SerializedName("photos")
    private PhotosInfo info;

    public PhotosInfo getInfo() {
        return info;
    }

    public void setInfo(PhotosInfo info) {
        this.info = info;
    }
}
