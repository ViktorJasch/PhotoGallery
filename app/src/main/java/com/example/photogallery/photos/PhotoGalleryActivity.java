package com.example.photogallery.photos;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import com.example.photogallery.SingleFragmentActivity;

public class PhotoGalleryActivity extends SingleFragmentActivity {
    private static final String TAG = "PhotoGalleryActivity";

    public static Intent newIntent(Context context){
        Intent i = new Intent(context, PhotoGalleryActivity.class);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

//    @Override
//    protected void onStart() {
//        Log.d(TAG, "onStart: ");
//        super.onStart();
//        Log.d(TAG, "onStart: post");
//    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        Log.d(TAG, "onResume: post");
    }

    @Override
    protected Fragment createFragment() {
        return new PhotoGalleryFragment();
    }
}
