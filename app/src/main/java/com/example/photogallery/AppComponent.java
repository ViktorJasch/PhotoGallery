package com.example.photogallery;

import com.example.photogallery.mvp.map.LocatrActivity;
import com.example.photogallery.mvp.map.LocatrComponent;
import com.example.photogallery.mvp.map.LocatrModule;
import com.example.photogallery.mvp.model.network.FlickrApi;
import com.example.photogallery.mvp.model.network.FlickrApiModule;
import com.example.photogallery.mvp.photos.PhotoGalleryActivity;
import com.example.photogallery.mvp.photos.PhotosComponent;
import com.example.photogallery.mvp.photos.PhotosModule;
import com.example.photogallery.service.PollServiceComponent;
import com.example.photogallery.service.PollServiceModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by viktor on 29.08.17.
 */

@Singleton
@Component(modules = {
        AppModule.class,
        FlickrApiModule.class
})
public interface AppComponent {
    LocatrComponent plus(LocatrModule module);
    PhotosComponent plus(PhotosModule module);
    PollServiceComponent plus(PollServiceModule module);
}
