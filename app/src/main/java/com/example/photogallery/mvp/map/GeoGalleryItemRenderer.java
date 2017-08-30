package com.example.photogallery.mvp.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.photogallery.PicassoHelper;
import com.example.photogallery.R;
import com.example.photogallery.mvp.model.GeoGalleryItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;


/**
 * Created by viktor on 30.08.17.
 */

public class GeoGalleryItemRenderer extends DefaultClusterRenderer<GeoGalleryItem> {
    private static final String TAG = "GeoGalleryItemRenderer";
    private IconGenerator mItemIconGenerator;
    private IconGenerator mClusterIconGenerator;
    private ImageView mItemView;
    private int dimension;
    private int padding;
    private PicassoHelper mPicassoHelper;

    //TODO заинжектить
    public GeoGalleryItemRenderer(Context context, ClusterManager<GeoGalleryItem> clusterManager,
                                  GoogleMap map, PicassoHelper picassoHelper){
        super(context, map, clusterManager);

        mItemIconGenerator = new IconGenerator(context);
        mClusterIconGenerator = new IconGenerator(context);


        dimension = (int) context.getResources().getDimension(R.dimen.custom_profile_image);
        padding = (int) context.getResources().getDimension(R.dimen.custom_profile_padding);
        mPicassoHelper = picassoHelper;

        mItemView = new ImageView(context);
        mItemView.setLayoutParams(new ViewGroup.LayoutParams(dimension, dimension));
        mItemView.setPadding(padding, padding, padding, padding);
        mItemIconGenerator.setContentView(mItemView);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<GeoGalleryItem> cluster) {
        return super.shouldRenderAsCluster(cluster);
    }

    @Override
    protected void onBeforeClusterItemRendered(GeoGalleryItem item, MarkerOptions markerOptions) {
        mPicassoHelper.putPhotoIntoView(mItemView, item);
        Bitmap icon = mItemIconGenerator.makeIcon();
        markerOptions
                .icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected void onBeforeClusterRendered(final Cluster<GeoGalleryItem> cluster, final MarkerOptions markerOptions) {
        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }
}
