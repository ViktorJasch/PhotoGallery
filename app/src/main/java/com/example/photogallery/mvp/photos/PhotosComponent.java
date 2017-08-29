package com.example.photogallery.mvp.photos;

import dagger.Component;
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
