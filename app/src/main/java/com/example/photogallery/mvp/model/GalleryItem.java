package com.example.photogallery.mvp.model;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

public class GalleryItem {
    @SerializedName("title")
    private String mCaption;
    @SerializedName("id")
    private String mId;
    @SerializedName("url_s")
    private String mUrl;
    @SerializedName("owner")
    private String mOwner;

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String owner) {
        this.mOwner = owner;
    }

    public Uri getPhotoUri(){
        return Uri.parse("http://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(mOwner)
                .appendPath(mId)
                .build();
    }

    @Override
    public String toString() {
        return mCaption;
    }

    @Override
    public boolean equals(Object obj) {
        GalleryItem item = (GalleryItem) obj;
        return mUrl.equals(item.getUrl()) &&
                mId.equals(item.getId());
    }

    @Override
    public int hashCode() {
        int res = 17;
        res = 37 * res + mUrl.hashCode();
        res = 37 * res + mId.hashCode();
        return res;
    }
}
