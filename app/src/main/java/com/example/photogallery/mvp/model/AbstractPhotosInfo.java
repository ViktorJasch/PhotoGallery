package com.example.photogallery.mvp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by viktor on 05.07.17.
 */

public abstract class AbstractPhotosInfo<T extends GalleryItem> {
    @SerializedName("page")
    private Integer page;
    @SerializedName("pages")
    private Integer pages;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public abstract List<T> getPhoto();
    public abstract void setPhoto(List<T> photo);
}
