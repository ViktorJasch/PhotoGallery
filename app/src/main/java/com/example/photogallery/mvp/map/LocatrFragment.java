package com.example.photogallery.mvp.map;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.photogallery.R;
import com.example.photogallery.mvp.model.GeoGalleryItem;
import com.example.photogallery.mvp.page.PhotoPageActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocatrFragment extends SupportMapFragment implements LocatrView, ClusterManager.OnClusterClickListener<GeoGalleryItem>, ClusterManager.OnClusterItemClickListener<GeoGalleryItem>{
    private LocatrPresenter presenter;
    private GoogleApiClient mClient;
    private GoogleMap mMap;
    private Location mCurrentLocation;
    private LatLngBounds.Builder boundsBuild;
    private ProgressDialog pd;
    private Map<GeoGalleryItem, Bitmap> hashMap;
    private ClusterManager<GeoGalleryItem> clusterManager;
    private GeoGalleryItem someItem;
    private static final String TAG = "LocatrFragment";

    public static LocatrFragment newInstance() {
        LocatrFragment fragment = new LocatrFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        hashMap = new HashMap<>(100);
        presenter = new LocatrPresenter(this);
        if(hashMap != null)
            hashMap.clear();
        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        getActivity().invalidateOptionsMenu();
                        loadData(false);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .build();

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                clusterInit();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_locatr, menu);

        MenuItem searchItem = menu.findItem(R.id.action_locate);
        searchItem.setEnabled(mClient.isConnected());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_locate:
                showPhoto();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
}

    @Override
    public void onStart() {
        super.onStart();
        getActivity().invalidateOptionsMenu();
        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mClient.disconnect();
    }

    @Override
    public void showLoading(boolean pullToRefresh) {
        pd = new ProgressDialog(getActivity());
        pd.show();
    }

    @Override
    public void showContent() {
        if(pd != null){
            pd.cancel();
            pd = null;
        }
    }

    @Override
    public void showError(Throwable e, boolean pullToRefresh) {
        Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_LONG);
    }

    @Override
    public void setData(List<GeoGalleryItem> data) {
        boundsBuild = new LatLngBounds.Builder();
        for(final GeoGalleryItem item : data){
            boundsBuild.include(new LatLng(item.getLat(), item.getLng()));
            hashMap.clear();
            Picasso.with(getActivity())
                    .load(item.getUrl())
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            hashMap.put(item, bitmap);
                            Log.d(TAG, "onBitmapLoaded: work is done");
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            Log.d(TAG, "onPrepareLoad: prepare");
                        }
                    });
        }
        clusterManager.addItems(data);
        clusterManager.cluster();
    }

    @Override
    public void loadData(boolean pullToRefresh) {
        if(mMap != null)
            mMap.clear();
        presenter.findImage(mClient);
    }

    @Override
    public boolean onClusterClick(Cluster<GeoGalleryItem> cluster) {
        return false;
    }

    @Override
    public boolean onClusterItemClick(GeoGalleryItem geoGalleryItem) {
        Intent intent = PhotoPageActivity.newIntent(getActivity(), geoGalleryItem.getPhotoUri());
        startActivity(intent);
        return false;
    }

    @Override
    public void setLocation(Location location) {
        mCurrentLocation = location;
    }

    private class GeoGalleryItemRenderer extends DefaultClusterRenderer<GeoGalleryItem>{
        private final IconGenerator itemIconGenerator;
        private final IconGenerator clusterIconGenerator;
        private final ImageView itemView;
        private final int dimension;
        private final int padding;

        public GeoGalleryItemRenderer(Context context){
            super(context, mMap, clusterManager);

            itemIconGenerator = new IconGenerator(context);
            clusterIconGenerator = new IconGenerator(context);

            dimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);

            itemView = new ImageView(context);
            itemView.setLayoutParams(new ViewGroup.LayoutParams(dimension, dimension));
            itemView.setPadding(padding, padding, padding, padding);
            itemIconGenerator.setContentView(itemView);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<GeoGalleryItem> cluster) {
            return super.shouldRenderAsCluster(cluster);
        }

        @Override
        protected void onBeforeClusterItemRendered(GeoGalleryItem item, MarkerOptions markerOptions) {
            //Log.d(TAG, "onBeforeClusterItemRendered: item bitmap is null " + (hashMap.get(item)==null));
            Picasso.with(getActivity())
                    .load(item.getUrl())
                    .into(itemView);

            Bitmap icon = itemIconGenerator.makeIcon();
            markerOptions
                    .icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected void onBeforeClusterRendered(final Cluster<GeoGalleryItem> cluster, final MarkerOptions markerOptions) {
            Bitmap icon = clusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }
    }

    private void clusterInit(){
        clusterManager = new ClusterManager<>(getActivity(), mMap);
        clusterManager.setRenderer(new GeoGalleryItemRenderer(getActivity()));
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);
        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);
    }

    private void showPhoto(){
        int margin = getResources().getDimensionPixelOffset(R.dimen.map_inset_margin);

        //Устанавливаем границы, по которым изначально отобразится карта
        LatLng myPoint = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        boundsBuild
                .include(myPoint);

        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(boundsBuild.build(), margin);
        mMap.animateCamera(update);

        Log.d(TAG, "updateUI: animateCameraCalled");
        MarkerOptions myMarker = new MarkerOptions()
                .position(myPoint);
        mMap.addMarker(myMarker);
    }
}
