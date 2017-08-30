package com.example.photogallery.mvp.photos.di;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;

import com.example.photogallery.IntentStarter;
import com.example.photogallery.PhotoAdapter;
import com.example.photogallery.PicassoHelper;
import com.example.photogallery.mvp.photos.CustomScrollListener;
import com.example.photogallery.mvp.photos.di.PhotosScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by viktor on 29.08.17.
 */

@Module
public class PhotosModule {

    @Provides
    @PhotosScope
    IntentStarter provideIntentStarter(){
        return new IntentStarter();
    }

//
//    @Provides
//    @PhotosScope
//    GridLayoutManager provideLayoutManager(){
//        return new GridLayoutManager(mPhotoGalleryFragment.getActivity(), 3);
//    }

}
