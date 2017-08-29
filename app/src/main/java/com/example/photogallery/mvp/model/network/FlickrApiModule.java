package com.example.photogallery.mvp.model.network;

import com.example.photogallery.Constants;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by viktor on 29.08.17.
 */

@Module
public class FlickrApiModule {
    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(){
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.connectTimeout(Constants.CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(Constants.READ_TIMEOUT_SEC, TimeUnit.SECONDS);

        return builder.build();
    }

    @Provides
    @Singleton
    public Retrofit provideRestAdapter(OkHttpClient client){
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(Constants.END_POINT)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client);

        return builder.build();
    }

    @Provides
    @Singleton
    public FlickrApi provideGithubApiService(Retrofit restAdapter) {
        return restAdapter.create(FlickrApi.class);
    }

    @Provides
    @Singleton
    public RequestsManager provideRequestsManager(FlickrApi service){
        return new RequestsManager(service);
    }
}
