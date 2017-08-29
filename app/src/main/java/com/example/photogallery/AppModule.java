package com.example.photogallery;

import android.app.Application;
import android.content.Context;

import com.example.photogallery.mvp.model.network.RequestsManager;

import javax.inject.Scope;
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
}
