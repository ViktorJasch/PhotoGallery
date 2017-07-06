package com.example.photogallery.mvp.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by viktor on 03.07.17.
 */

public class Photos<T extends AbstractPhotosInfo> {
    @SerializedName("photos")
    private T info;

    public T getInfo() {
        return info;
    }

    public void setInfo(T info) {
        this.info = info;
    }
}
