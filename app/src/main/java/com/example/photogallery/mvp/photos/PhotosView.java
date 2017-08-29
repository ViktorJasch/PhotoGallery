package com.example.photogallery.mvp.photos;

import com.example.photogallery.mvp.model.GalleryItem;
import com.example.photogallery.permissions.BasePermissionDefinition;
import com.hannesdorfmann.mosby3.mvp.lce.MvpLceView;

import java.util.List;

/**
 * Created by viktor on 02.07.17.
 */

public interface PhotosView extends MvpLceView<List<GalleryItem>> {
    void requestAndExplainPermission(BasePermissionDefinition basePermissionDefinition);
}
