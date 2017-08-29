package com.example.photogallery.mvp.map;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.example.photogallery.SingleFragmentActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class LocatrActivity extends SingleFragmentActivity {
    private final String TAG_FRAGMENT_LOCATR = "locatrFragment";
    private static final int REQUEST_CODE = 0;
    private static final String TAG = "LocatrActivity";
    private OnBackPressedListener listener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_LOCATR);
        Log.d(TAG, "onCreate: fragment = null " + (fragment == null));
        listener = (OnBackPressedListener) fragment;
    }

    @Override
    protected Fragment createFragment() {
        setFragmentTag(TAG_FRAGMENT_LOCATR);
        Fragment fragment = LocatrFragment.newInstance();
        listener = (OnBackPressedListener) fragment;
        return fragment;
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

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: listener = null " + (listener == null));
        if(listener != null){
            if(!listener.onBackPressed())
                super.onBackPressed();
        }
    }

    public static Intent newIntent(Context context){
        Intent i = new Intent(context, LocatrActivity.class);
        return i;
    }

    //Для обработки событий onBackPressed во фрагментах
    public interface OnBackPressedListener {
        //если фрагменту не нужно обробатывать нажатие, то возвращается false.
        //Активность в этом случае вызывает метод родителя
        boolean onBackPressed();
    }
}
