package com.example.photogallery.app;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.example.photogallery.PhotoAdapter;
import com.example.photogallery.PicassoHelper;
import com.example.photogallery.QueryPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by viktor on 29.08.17.
 */

@Module
public class AppModule {
    private Context mContext;

    public AppModule(Context application){
        mContext = application;
    }

    @Provides
    @Singleton
    public Context provideApplication(){
        return mContext;
    }

    @Provides
    @Singleton
    public QueryPreferences provideQueryPreferences(Context context){
        return new QueryPreferences(context);
    }

    @Provides
    public PicassoHelper providePicassoHelper(Context context){
        return new PicassoHelper(context);
    }

    //TODO вынести провайдер в отдельный компонент для списков
    @Provides
    PhotoAdapter provideRecycleViewAdapter(PicassoHelper picassoHelper, Context context){
        return new PhotoAdapter(picassoHelper, context);
    }
}
