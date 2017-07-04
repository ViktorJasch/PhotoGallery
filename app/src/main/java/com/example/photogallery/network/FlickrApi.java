package com.example.photogallery.network;

import com.example.photogallery.model.Photos;
import com.example.photogallery.model.PhotosInfo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by viktor on 03.07.17.
 */

public interface FlickrApi {

    @GET("?method=flickr.photos.getRecent")
    Call<Photos> getRecentPhotos(
            @Query("api_key") String apiKey, @Query("extras") String text,
            @Query("format") String format, @Query("nojsoncallback") String num);

    @GET("?method=flickr.photos.search")
    Call<Photos> searchPhoto(
            @Query("api_key") String apiKey, @Query("extras") String text, @Query("text") String query,
            @Query("format") String format, @Query("nojsoncallback") String num);
}
