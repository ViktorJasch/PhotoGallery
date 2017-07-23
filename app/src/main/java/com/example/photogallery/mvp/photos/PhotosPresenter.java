package com.example.photogallery.mvp.photos;

import android.util.Log;

import com.example.photogallery.mvp.model.GalleryItem;
import com.example.photogallery.network.Requests;
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by viktor on 02.07.17.
 */

public class PhotosPresenter extends MvpBasePresenter<PhotosView> {
    private static final String TAG = "PhotosPresenter";
    private List<GalleryItem> items = new ArrayList<>(128);
    private int searchPage = 1;
    private int recentPage = 1;

    public void loadMore(String query){
        Log.d(TAG, "loadMore: ");
        getView().showLoading(false);
        if(query == null){
            recentPage++;
            getRecentPhoto(recentPage, false);
        }
        else{
            searchPage++;
            searchPhoto(query, searchPage, false);
        }
    }

    public void loadItems(String query, boolean pullToRefresh){
        Log.d(TAG, "loadItems: ");
        getView().showLoading(pullToRefresh);
        if(query == null){
            getRecentPhoto(recentPage = 1, pullToRefresh);
        }
        else{
            searchPhoto(query, searchPage = 1, pullToRefresh);
        }
    }

    private void getRecentPhoto(final int page, final boolean pullToRefresh){
        Log.d(TAG, "getRecentPhoto: called, page = " + page);
        Requests.getRecentPhoto(page).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(photosInfoPhotos -> photosInfoPhotos.getInfo().getPhoto())
                .subscribe(new Observer<List<GalleryItem>>() {
                    @Override
                    public void onCompleted() {
                        getView().setData(items);
                        getView().showContent();
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().showError(e, false);
                    }

                    @Override
                    public void onNext(List<GalleryItem> photos) {
                        Collections.reverse(photos);
                        doOnResponce(photos, page, pullToRefresh);
                    }
                });
    }

    private void searchPhoto(String query, final int page, final boolean pullToRefresh){
        Requests.searchPhoto(query, page).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(photosInfoPhotos -> photosInfoPhotos.getInfo().getPhoto())
                .subscribe(new Observer<List<GalleryItem>>() {
                    @Override
                    public void onCompleted() {
                        getView().setData(items);
                        getView().showContent();
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().showError(e, false);
                    }

                    @Override
                    public void onNext(List<GalleryItem> photos) {
                        Collections.reverse(photos);
                        doOnResponce(photos, page, pullToRefresh);
                    }
                });
    }

    private void doOnResponce(List<GalleryItem> freshItems, int page, boolean pullToRefresh) {
        if(page == 1 || pullToRefresh){
            items = freshItems;
        }
        else{
            items.addAll(freshItems);
        }
    }
}
