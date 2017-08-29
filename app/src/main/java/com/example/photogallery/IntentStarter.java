package com.example.photogallery;

import android.content.Context;
import android.content.Intent;

import com.example.photogallery.mvp.map.LocatrActivity;

/**
 * Created by viktor on 29.08.17.
 */

public class IntentStarter {
    public void showLocatr(Context context){
        Intent intent = LocatrActivity.newIntent(context);
        context.startActivity(intent);
    }
}
