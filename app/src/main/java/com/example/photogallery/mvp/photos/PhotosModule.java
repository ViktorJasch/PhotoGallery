package com.example.photogallery.mvp.photos;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.photogallery.IntentStarter;
import com.example.photogallery.PhotoAdapter;
import com.example.photogallery.QueryPreferences;

import dagger.Module;
import dagger.Provides;

/**
 * Created by viktor on 29.08.17.
 */

@Module
public class PhotosModule {
    private CustomScrollListener.LoadingListener mLoadingListener;

    PhotosModule(CustomScrollListener.LoadingListener loadingListener){
        mLoadingListener = loadingListener;
    }

    @Provides
    @PhotosScope
    IntentStarter provideIntentStarter(){
        return new IntentStarter();
    }

    @Provides
    @PhotosScope
    PhotoAdapter provideRecycleViewAdapter(Context context){
        return new PhotoAdapter(context);
    }

    @Provides
    @PhotosScope
    GridLayoutManager provideLayoutManager(Context context){
        return new GridLayoutManager(context, 3);
    }

    @Provides
    @PhotosScope
    CustomScrollListener provideRecycleViewScrollListener(GridLayoutManager gridLayoutManager){
        return new CustomScrollListener(gridLayoutManager, mLoadingListener);
    }

//    @Provides
//    @PhotosScope
//    PhotoAdapter provideRecycleViewAdapter(){
//        return new PhotoAdapter(mPhotoGalleryFragment.getActivity());
//    }
//
//    @Provides
//    @PhotosScope
//    GridLayoutManager provideLayoutManager(){
//        return new GridLayoutManager(mPhotoGalleryFragment.getActivity(), 3);
//    }

}
