package com.example.photogallery.mvp.web_page;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

import com.example.photogallery.SingleFragmentActivity;

public class PhotoPageActivity extends SingleFragmentActivity {
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
