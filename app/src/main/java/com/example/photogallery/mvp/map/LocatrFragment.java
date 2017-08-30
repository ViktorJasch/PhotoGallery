package com.example.photogallery.mvp.map;


import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.Toast;

import com.example.photogallery.PhotoAdapter;
import com.example.photogallery.app.PhotoGalleryApp;
import com.example.photogallery.PicassoHelper;
import com.example.photogallery.R;
import com.example.photogallery.bus.PhotoHolderClickedEvent;
import com.example.photogallery.mvp.map.di.LocatrComponent;
import com.example.photogallery.mvp.map.di.LocatrModule;
import com.example.photogallery.mvp.model.GeoGalleryItem;
import com.example.photogallery.mvp.page.PhotoPageActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.hannesdorfmann.mosby3.mvp.MvpFragment;
import com.sothree.slidinguppanel.ScrollableViewHelper;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */

//TODO Реализовать EventBus на RxJava
public class LocatrFragment extends MvpFragment<LocatrView, LocatrPresenter> implements LocatrView,
        ClusterManager.OnClusterClickListener<GeoGalleryItem>,
        ClusterManager.OnClusterItemClickListener<GeoGalleryItem>,
        LocatrActivity.OnBackPressedListener,
        GoogleMap.OnMapClickListener{
    @BindView(R.id.mapView)
    MapView mMapView;
    @BindView(R.id.clusterList)
    RecyclerView mClusterList;
    @BindView(R.id.slidingLayout)
    SlidingUpPanelLayout mSlidingLayout;

    @Inject PhotoAdapter adapter;
    private boolean slidingPanelIsHidden;
    private LocatrComponent mLocatrComponent;
    private GoogleMap mMap;
    private ProgressDialog pd;
    private ClusterManager<GeoGalleryItem> clusterManager;
    private static final String TAG = "LocatrFragment";

    public static LocatrFragment newInstance() {
        LocatrFragment fragment = new LocatrFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initComponent();
        setRetainInstance(true);
        setHasOptionsMenu(true);
        Log.d(TAG, "onCreate: ");
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
        mClusterList.setLayoutManager(layoutManager);
        mClusterList.setAdapter(adapter);
        mSlidingLayout.setScrollableViewHelper(new ScrollableViewHelper());
        mSlidingLayout.setAnchorPoint(0.7f);
        mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        mMapView.onCreate(bundle);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.onMapViewCreate(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_locatr, menu);

        MenuItem searchItem = menu.findItem(R.id.action_locate);
        searchItem.setEnabled(presenter.isGoogleApiClientConnected());
    }

    @Override
    public LocatrPresenter createPresenter() {
        return mLocatrComponent.presenter();
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

    //region Жизненный цикл для mMapView
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
        presenter.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
        presenter.onStop();
        EventBus.getDefault().unregister(this);
    }
    //endregion

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
        clusterManager.addItems(data);
        clusterManager.cluster();
    }

    @Override
    public void loadData(boolean pullToRefresh) {
        if(mMap != null)
            mMap.clear();
        presenter.findImage();
    }

    @Override
    public void invalidateMenu() {
        getActivity().invalidateOptionsMenu();
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

    private void clusterInit(){
        clusterManager = new ClusterManager<>(getActivity(), mMap);
        clusterManager.setRenderer(new GeoGalleryItemRenderer(getActivity(), clusterManager, mMap,
                new PicassoHelper(getActivity())));
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

    @Subscribe
    public void onPhotoHolderClickedEvent(PhotoHolderClickedEvent event){
        Intent i = PhotoPageActivity
                .newIntent(getActivity(), event.getPhotoUri());
        startActivity(i);
    }

    private void initComponent(){
        mLocatrComponent = PhotoGalleryApp.get(getActivity()).getAppComponent()
                .plus(new LocatrModule());
        mLocatrComponent.inject(this);
    }
}
