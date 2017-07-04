package com.example.photogallery.photos;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.photogallery.model.GalleryItem;
import com.example.photogallery.R;
import com.example.photogallery.ThumbnailDownload;
import com.example.photogallery.bus.PhotoHolderClickedEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by viktor on 02.07.17.
 */

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoHolder>{
    List<GalleryItem> galleryItems = new ArrayList<>();
    ThumbnailDownload<PhotoHolder> thumbnailDownload;
    Drawable defaultDrawable;

    public PhotoAdapter(ThumbnailDownload thumbnailDownload, Drawable defaultDrawable){
        this.thumbnailDownload = thumbnailDownload;
        this.defaultDrawable = defaultDrawable;
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
        GalleryItem galleryItem = galleryItems.get(position);
        holder.bindImage(defaultDrawable);
        holder.bindGalleryItem(galleryItem);

        thumbnailDownload.queueThumbnail(holder, galleryItem.getUrl());
    }

    @Override
    public int getItemCount() {
        return galleryItems.size();
    }

    public void setPhotos(List<GalleryItem> items){
        galleryItems = items;
    }

     class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView mPhoto;
        private GalleryItem mGalleryItem;

        public PhotoHolder(View itemView) {
            super(itemView);

            mPhoto = (ImageView) itemView;
        }

        public void bindImage(Drawable item){
            mPhoto.setImageDrawable(item);
        }

        public void bindGalleryItem(GalleryItem item){
            mGalleryItem = item;
        }

        @Override
        public void onClick(View v) {
            EventBus.getDefault().post(new PhotoHolderClickedEvent(mGalleryItem.getPhotoUri()));
        }
    }
}
