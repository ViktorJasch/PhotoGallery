package com.example.photogallery.network;

import android.util.Log;

import com.example.photogallery.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by viktor on 03.07.17.
 */

public class RetrofitClient {
    static {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(Constants.END_POINT)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(httpClient.build());

        retrofit = builder.build();
    }
    private static final String TAG = "RetrofitClient";
    private static Retrofit retrofit;

    public static <S> S getService(Class<S> serviceClass){
        return retrofit.create(serviceClass);
    }
}
