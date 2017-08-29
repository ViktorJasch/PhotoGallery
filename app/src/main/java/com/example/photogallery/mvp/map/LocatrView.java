package com.example.photogallery.mvp.map;

import com.example.photogallery.mvp.model.GeoGalleryItem;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hannesdorfmann.mosby3.mvp.MvpView;
import com.hannesdorfmann.mosby3.mvp.lce.MvpLceView;

import java.util.List;

public interface LocatrView extends MvpView {
    void showPhoto(CameraUpdate update, MarkerOptions myMarker);
    void loadData(boolean pullToRefresh);
    void setData(List<GeoGalleryItem> data);
    void showError(Throwable e, boolean pullToRefresh);
    void showContent();
    void showLoading(boolean pullToRefresh);
}
