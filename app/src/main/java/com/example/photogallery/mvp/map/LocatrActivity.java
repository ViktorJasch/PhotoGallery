package com.example.photogallery.mvp.map;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.example.photogallery.SingleFragmentActivity;
import com.example.photogallery.mvp.page.PhotoPageActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

//TODO добавить динамическое добавление разрешений
public class LocatrActivity extends SingleFragmentActivity {
    private static final int REQUEST_CODE = 0;
    private static final String TAG = "LocatrActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment createFragment() {
        return LocatrFragment.newInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GoogleApiAvailability gapa = GoogleApiAvailability.getInstance();
        int errorCode = gapa
                .isGooglePlayServicesAvailable(this);

        if(errorCode != ConnectionResult.SUCCESS){
            Dialog errorDialog = gapa.getErrorDialog(this, errorCode, REQUEST_CODE,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            //если сервис недоступен - выходим.
                            finish();
                        }
                    });
            errorDialog.show();
        }
    }

    public static Intent newIntent(Context context){
        Intent i = new Intent(context, LocatrActivity.class);
        return i;
    }
}
