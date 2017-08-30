package com.example.photogallery;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.example.photogallery.mvp.model.GalleryItem;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by viktor on 30.08.17.
 */

public class PicassoHelper<T extends GalleryItem> {
    private Context mContext;
    private Drawable mPlaceholder;

    public PicassoHelper(Context context){
        mContext = context;
    }

    public void cechingPhotos(List<T> data){
        for (GalleryItem item :
                data) {
            Picasso.with(mContext)
                    .load(item.getUrl())
                    .fetch();
        }
    }

    public void putPhotoIntoView(ImageView target, T item){
        Picasso.with(mContext)
                .load(item.getUrl())
                .placeholder(mPlaceholder)
                .into(target);
    }

    public void setPlaceholder(Drawable placeholder){
        mPlaceholder = placeholder;
    }
}
