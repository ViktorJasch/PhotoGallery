package com.example.photogallery;

import android.app.Application;
import android.content.Context;

/**
 * Created by viktor on 29.08.17.
 */

public class PhotoGalleryApp extends Application{

    private AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        initAppComponent();
    }

    private void initAppComponent(){
        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public static PhotoGalleryApp get(Context context){
        return (PhotoGalleryApp) context.getApplicationContext();
    }

    public AppComponent getAppComponent(){
        return mAppComponent;
    }
}
