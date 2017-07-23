package com.example.photogallery.network;

import com.example.photogallery.mvp.model.GalleryItem;
import com.example.photogallery.mvp.model.GeoPhotosInfo;
import com.example.photogallery.mvp.model.Photos;
import com.example.photogallery.mvp.model.PhotosInfo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by viktor on 03.07.17.
 */

public interface FlickrApi {

    @GET("?method=flickr.photos.getRecent")
    Observable<Photos<PhotosInfo>> getRecentPhotos(
            @Query("api_key") String apiKey, @Query("extras") String extras , @Query("page") int page,
            @Query("format") String format, @Query("nojsoncallback") String nojsoncallback);

    @GET("?method=flickr.photos.search")
    Observable<Photos<PhotosInfo>> searchPhoto(
            @Query("api_key") String apiKey, @Query("extras") String extras, @Query("page") int page,
            @Query("text") String query, @Query("format") String format, @Query("nojsoncallback") String nojsoncallback);

    @GET("?method=flickr.photos.search")
    Observable<Photos<GeoPhotosInfo>> searchGeoPhoto(
            @Query("api_key") String apiKey, @Query("extras") String extras, @Query("lat") String lat,
            @Query("lon") String lon, @Query("format") String format, @Query("nojsoncallback") String nojsoncallback);
}
