package com.example.photogallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Виктор on 15.10.2016.
 */

//этот абстрактный класс реализует интерфейс создания любого фрагмента
public abstract class SingleFragmentActivity extends AppCompatActivity {
    private static final String TAG = "SingleFragmentActivity";
    private String fragmentTag = null;

    protected abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        Log.d(TAG, "onCreate: setContentViewPast");
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if(fragment == null){
            fragment = createFragment();
            FragmentTransaction ft =  fm.beginTransaction();
            if(fragmentTag != null){
                ft.add(R.id.fragment_container, fragment, fragmentTag);
                Log.d(TAG, "onCreate: fragment with tag");
            }
            else
                ft.add(R.id.fragment_container, fragment);

            ft.commit();
            fm.executePendingTransactions();
        }
    }

    public void setFragmentTag(String fragmentTag) {
        this.fragmentTag = fragmentTag;
    }
}
