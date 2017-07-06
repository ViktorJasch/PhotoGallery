package com.example.photogallery.network;

import com.example.photogallery.mvp.model.GalleryItem;
import com.example.photogallery.mvp.model.GeoPhotosInfo;
import com.example.photogallery.mvp.model.Photos;
import com.example.photogallery.mvp.model.PhotosInfo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by viktor on 03.07.17.
 */

public interface FlickrApi {

    @GET("?method=flickr.photos.getRecent")
    Call<Photos<PhotosInfo>> getRecentPhotos(
            @Query("api_key") String apiKey, @Query("extras") String text,
            @Query("format") String format, @Query("nojsoncallback") String num);

    @GET("?method=flickr.photos.search")
    Call<Photos<PhotosInfo>> searchPhoto(
            @Query("api_key") String apiKey, @Query("extras") String text, @Query("text") String query,
            @Query("format") String format, @Query("nojsoncallback") String num);

    @GET("?method=flickr.photos.search")
    Call<Photos<GeoPhotosInfo>> searchGeoPhoto(
            @Query("api_key") String apiKey, @Query("extras") String extras, @Query("lat") String lat,
            @Query("lon") String lon, @Query("format") String format, @Query("nojsoncallback") String num);
}
