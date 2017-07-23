package com.example.photogallery.mvp.map;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.photogallery.PhotoAdapter;
import com.example.photogallery.R;
import com.example.photogallery.bus.PhotoHolderClickedEvent;
import com.example.photogallery.mvp.model.GeoGalleryItem;
import com.example.photogallery.mvp.page.PhotoPageActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.sothree.slidinguppanel.ScrollableViewHelper;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocatrFragment extends Fragment implements LocatrView, ClusterManager.OnClusterClickListener<GeoGalleryItem>,
        ClusterManager.OnClusterItemClickListener<GeoGalleryItem>, LocatrActivity.OnBackPressedListener,
        GoogleMap.OnMapClickListener{
    @BindView(R.id.mapView)
    MapView mMapView;
    @BindView(R.id.clusterList)
    RecyclerView mClusterList;
    @BindView(R.id.slidingLayout)
    SlidingUpPanelLayout mSlidingLayout;

    private boolean slidingPanelIsHidden;
    private PhotoAdapter adapter;
    private LocatrPresenter presenter;
    private GoogleApiClient mClient;
    private GoogleMap mMap;
    private ProgressDialog pd;
    private Map<GeoGalleryItem, Bitmap> hashMap;
    private ClusterManager<GeoGalleryItem> clusterManager;
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
        Log.d(TAG, "onCreate: ");
        hashMap = new HashMap<>(128);
        presenter = new LocatrPresenter(this);
        googleApiClientInit();
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_app_map, viewGroup, false);
        ButterKnife.bind(this, view);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                clusterInit();
            }
        });
        Log.d(TAG, "onCreateView: mMapView = null: " + (mMapView == null));
        slidingPanelIsHidden = true;
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        adapter = new PhotoAdapter<>(getActivity());
        mClusterList.setLayoutManager(layoutManager);
        mClusterList.setAdapter(adapter);
        mSlidingLayout.setScrollableViewHelper(new ScrollableViewHelper());
        mSlidingLayout.setAnchorPoint(0.7f);
        mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        mMapView.onCreate(bundle);

        return view;
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
                presenter.updateLocation(getResources().getDimensionPixelOffset(R.dimen.map_inset_margin));
                return true;
            case R.id.action_photo_list:
                mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                slidingPanelIsHidden = false;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Bundle mapState = new Bundle();
        mMapView.onSaveInstanceState(mapState);
        super.onSaveInstanceState(outState);
    }

    //Жизненный цикл для mMapView
    @Override
    public void onResume() {
        Log.d(TAG, "onResume: mMapView = null: " + (mMapView == null));
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
    @Override
    public void onStart() {
        super.onStart();
        getActivity().invalidateOptionsMenu();
        mClient.connect();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
        mClient.disconnect();
        EventBus.getDefault().unregister(this);
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
        adapter.setPhotos(data);
        adapter.notifyDataSetChanged();
        hashMap.clear();
        for(final GeoGalleryItem item : data){
            Picasso.with(getActivity())
                    .load(item.getUrl())
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            hashMap.put(item, bitmap);
                            Log.d(TAG, "onBitmapLoaded: done");
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
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
        adapter.setPhotos(new ArrayList(cluster.getItems()));
        adapter.notifyDataSetChanged();
        mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
        slidingPanelIsHidden = false;
        return false;
    }

    @Override
    public boolean onClusterItemClick(GeoGalleryItem geoGalleryItem) {
        Intent intent = PhotoPageActivity.newIntent(getActivity(), geoGalleryItem.getPhotoUri());
        startActivity(intent);
        return false;
    }

    @Override
    public boolean onBackPressed() {
        if(!slidingPanelIsHidden){
            mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            slidingPanelIsHidden = true;
            return true;
        }
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        slidingPanelIsHidden = true;
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
            Log.d(TAG, "onBeforeClusterItemRendered: item bitmap is null " + (hashMap.get(item)==null));
            if(hashMap.get(item) == null){
                Picasso.with(getActivity())
                        .load(item.getUrl())
                        .into(itemView);
            } else {
                itemView.setImageBitmap(hashMap.get(item));
            }

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
        mMap.setOnMapClickListener(this);
        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);
    }

    @Override
    public void showPhoto(CameraUpdate update, MarkerOptions myMarker){
        mMap.animateCamera(update);
        mMap.addMarker(myMarker);
    }

    private void googleApiClientInit() {
        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(TAG, "onConnected: ");
                        getActivity().invalidateOptionsMenu();
                        loadData(false);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .build();
    }

    @Subscribe
    public void onPhotoHolderClickedEvent(PhotoHolderClickedEvent event){
        Intent i = PhotoPageActivity
                .newIntent(getActivity(), event.getPhotoUri());
        startActivity(i);
    }
}
