package com.example.photogallery.mvp.model.network;

import com.example.photogallery.Constants;

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
