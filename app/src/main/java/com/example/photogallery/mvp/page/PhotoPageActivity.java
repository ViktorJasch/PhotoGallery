package com.example.photogallery.mvp.page;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

import com.example.photogallery.SingleFragmentActivity;
import com.example.photogallery.onBackPressedListener;

public class PhotoPageActivity extends SingleFragmentActivity {
    /**
     * ЗАМЕЧАНИЕ! Эту активность нежелательно использовать в качестве родителя нескольких фрагментов
     * в данной реализации.
     * Если
     */
    private onBackPressedListener mOnBackPressedListener;

    public static Intent newIntent(Context context, Uri photoPageUri){
        Intent i = new Intent(context, PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        //В Activity хранится экземпляр OnBackPressedListener
        Fragment fragment = PhotoPageFragment.newInstance(getIntent().getData());
        if(fragment instanceof onBackPressedListener)
            mOnBackPressedListener = (onBackPressedListener) fragment;
        return fragment;
    }

    @Override
    public void onBackPressed() {
        if(mOnBackPressedListener == null)
            return;

        if(mOnBackPressedListener.onBackPressed())
            return;
        else
            super.onBackPressed();
    }
}
