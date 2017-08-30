package com.example.photogallery.mvp.photos.di;

import com.example.photogallery.mvp.photos.PhotoGalleryFragment;
import com.example.photogallery.mvp.photos.PhotosPresenter;

import dagger.Subcomponent;

/**
 * Created by viktor on 29.08.17.
 */

@PhotosScope
@Subcomponent(modules = PhotosModule.class)
public interface PhotosComponent {
    void inject(PhotoGalleryFragment photoGalleryFragment);
    PhotosPresenter presenter();
}
