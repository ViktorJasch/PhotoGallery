package com.example.photogallery.mvp.photos;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.photogallery.Constants;
import com.example.photogallery.IntentStarter;
import com.example.photogallery.QueryPreferences;
import com.example.photogallery.mvp.model.GalleryItem;
import com.example.photogallery.mvp.model.network.RequestsManager;
import com.example.photogallery.permissions.FineLocationPermissionDefinition;
import com.example.photogallery.service.PollService;
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by viktor on 02.07.17.
 */

public class PhotosPresenter extends MvpBasePresenter<PhotosView> {
    private static final String TAG = "PhotosPresenter";
    private ArrayList<GalleryItem> mItems = new ArrayList<>(128);
    @Inject QueryPreferences mQueryPreferences;
    @Inject RequestsManager mRequestsManager;
    @Inject IntentStarter mIntentStarter;
    //Возможно два вида загрузки фото: загрузка по тематике пользователя (если он что то ввел с троку search)
    // и загрузка последних фотографий.
    //В связи с тем, что реализованна пагинация (показать еще), а flickr выдает фото постранично,
    //нужно отслеживать эти страницы. При переключении между различными видами загрузки,
    //необходимо устанавливать текущую страницу в 1;
    private int searchPage = 1;
    private int recentPage = 1;

    @Inject
    PhotosPresenter(){
    }

    public void onQuerySet(String query){
        mQueryPreferences.setStoredQuery(query);
        searchPhoto(query, searchPage = 1, false);
    }

    public void onClearItemClick(){
        mQueryPreferences.setStoredQuery(null);
        getRecentPhoto(recentPage = 1, false);
    }

    public String getQueryString(){
        return mQueryPreferences.getStoredQuery();
    }

    public void onActionLocateItemClick(Context context){
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "loadData: if permissions need");
            getView().requestAndExplainPermission(new FineLocationPermissionDefinition());
        } else{
            mIntentStarter.showLocatr(context);
        }
    }

    public void onRequestPermissionResult(int requestCode, int[] result, Context context){
        switch (requestCode){
            case Constants.PERMISSION_ACCESS_FINE_LOCATION:
                if (result[0] == PackageManager.PERMISSION_GRANTED)
                    mIntentStarter.showLocatr(context);
        }
    }

    public void onTogglePollingItemClick(Context context){
        boolean ShouldStartAlarm = PollService.isServiceAlarmOn(context);
        PollService.setServiceAlarm(context, !ShouldStartAlarm);
        mQueryPreferences.setAlarmOn(!ShouldStartAlarm);
    }

    //TODO loadMore и loadItems похожи. Сделать код с проверками читабельней
    public void loadMore(){
        Log.d(TAG, "loadMore: ");
        getView().showLoading(false);
        String query = mQueryPreferences.getStoredQuery();
        if(query == null){
            recentPage++;
            getRecentPhoto(recentPage, false);
        }
        else{
            searchPage++;
            searchPhoto(query, searchPage, false);
        }
    }

    public void loadItems(boolean pullToRefresh){
        Log.d(TAG, "loadItems: ");
        getView().showLoading(pullToRefresh);
        String query = mQueryPreferences.getStoredQuery();
        if(query == null){
            getRecentPhoto(recentPage = 1, pullToRefresh);
        }
        else{
            searchPhoto(query, searchPage = 1, pullToRefresh);
        }
    }

    private void getRecentPhoto(final int page, final boolean pullToRefresh){
        Log.d(TAG, "getRecentPhoto: called, page = " + page);
        mRequestsManager.getRecentPhoto(page).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(photosInfoPhotos -> photosInfoPhotos.getInfo().getPhoto())
                .subscribe(photos -> doNext(photos, page, pullToRefresh),
                        throwable -> doError(throwable),
                        () -> doComplited());
    }

    private void searchPhoto(String query, final int page, final boolean pullToRefresh){
        mRequestsManager.searchPhoto(query, page).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(photosInfoPhotos -> photosInfoPhotos.getInfo().getPhoto())
                .subscribe(photos -> doNext(photos, page, pullToRefresh),
                        throwable -> doError(throwable),
                        () -> doComplited());
    }

    private void doResponce(List<GalleryItem> freshItems, int page, boolean pullToRefresh) {
        if(pullToRefresh){
            mItems.addAll(0, freshItems);
        }
        else if(page == 1){
            mItems.clear();
            mItems.addAll(freshItems);
        }
        else{
            mItems.addAll(freshItems);
        }
    }

    private void doComplited(){
        getView().setData(mItems);
        getView().showContent();
    }

    private void doError(Throwable throwable){
        getView().showError(throwable, false);
    }

    private void doNext(List<GalleryItem> photos, int page, boolean pullToRefresh){
        Collections.reverse(photos);
        doResponce(photos, page, pullToRefresh);
    }
}
