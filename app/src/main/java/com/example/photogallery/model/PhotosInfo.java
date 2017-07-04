package com.example.photogallery.model;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.Primitives;

import java.util.List;

/**
 * Created by viktor on 03.07.17.
 */

public class PhotosInfo {
    @SerializedName("page")
    private Integer page;
    @SerializedName("pages")
    private Integer pages;
    @SerializedName("photo")
    private List<GalleryItem> photo;

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

    public List<GalleryItem> getPhoto() {
        return photo;
    }

    public void setPhoto(List<GalleryItem> photo) {
        this.photo = photo;
    }
}
