package com.example.photogallery;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.photogallery.bus.PhotoHolderClickedEvent;
import com.example.photogallery.mvp.model.*;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by viktor on 02.07.17.
 * Закомментированные строчки - рудимент, оставшийся просто для вспоминания того,
 * как работал проект вместе с HandlerThread
 */

public class PhotoAdapter<T extends GalleryItem> extends RecyclerView.Adapter<PhotoAdapter<T>.PhotoHolder>{
    List<T> galleryItems = new ArrayList<>();
    private PicassoHelper mPicassoHelper;


    public PhotoAdapter(PicassoHelper picassoHelper, Context context){
        mPicassoHelper = picassoHelper;
        mPicassoHelper.setPlaceholder(context.getResources().getDrawable(R.drawable.bill_up_close));
    }

    @Override
    public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater
                .from(parent.getContext());
        View view = inflater.inflate(R.layout.gallery_item, parent, false);
        PhotoHolder ph = new PhotoHolder(view);
        view.setOnClickListener(ph);
        return ph;
    }

    @Override
    public void onBindViewHolder(PhotoHolder holder, int position) {
        T galleryItem = galleryItems.get(position);
        //holder.bindImage(defaultDrawable);
        holder.bindGalleryItem(galleryItem);

        //thumbnailDownload.queueThumbnail(holder, galleryItem.getUrl());
    }

    @Override
    public int getItemCount() {
        return galleryItems.size();
    }

    public void setPhotos(List<T> items){
        galleryItems = items;
    }

    class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView mPhoto;
        private T galleryItem;

        public PhotoHolder(View itemView) {
            super(itemView);

            mPhoto = (ImageView) itemView;
        }

        public void bindImage(Drawable item){
            mPhoto.setImageDrawable(item);
        }

        public void bindGalleryItem(T item){
            galleryItem = item;
            mPicassoHelper.putPhotoIntoView(mPhoto, item);
        }

        //TODO увеличивать по клику изображение (с анимацией)
        @Override
        public void onClick(View v) {
            EventBus.getDefault().post(new PhotoHolderClickedEvent(galleryItem.getPhotoUri()));
        }
    }
}
