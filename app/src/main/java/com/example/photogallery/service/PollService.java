package com.example.photogallery.service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.photogallery.PhotoGalleryApp;
import com.example.photogallery.QueryPreferences;
import com.example.photogallery.R;
import com.example.photogallery.mvp.model.GalleryItem;
import com.example.photogallery.mvp.model.network.RequestsManager;
import com.example.photogallery.mvp.photos.PhotoGalleryActivity;
import com.example.photogallery.mvp.photos.PhotosModule;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by Виктор on 11.01.2017.
 */

public class PollService extends IntentService {
    private static String TAG = "PollService";
    private static final long POLL_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    public static final String ACTION_SHOW_NOTIFICATION = "com.example.photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE = "com.example.photogallery.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";


    @Inject QueryPreferences queryPreferences;
    @Inject RequestsManager requestsManager;
    public PollService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PhotoGalleryApp.get(this).getAppComponent()
                .plus(new PollServiceModule());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(!isNetworkAvailableAndConnected())
            return;

        String query = queryPreferences.getStoredQuery();
        final String lastResultId = queryPreferences.getLastResultId();

        if(query == null)
            requestsManager.getRecentPhoto(1)
                    //map - получаем из PhotosInfo лист элементов GalleryItem
                    .map(photosInfoPhotos -> photosInfoPhotos.getInfo().getPhoto())
                    .subscribe(photosInfoPhotos -> doOnResponse(photosInfoPhotos, lastResultId),
                    throwable -> throwable.printStackTrace());
        else
            requestsManager.searchPhoto(query, 1)
                    .map(photosInfoPhotos -> photosInfoPhotos.getInfo().getPhoto())
                    .subscribe(photosInfoPhotos -> doOnResponse(photosInfoPhotos, lastResultId),
                    throwable -> throwable.printStackTrace());
    }

    private void doOnResponse(List<GalleryItem> photos, String lastResultId) {
        if(photos.size() == 0)
            return;

        String resultId = photos.get(0).getId();
        if(resultId.equals(lastResultId))
            Log.i(TAG, "onHandleIntent: Got an old result " + resultId);
        else{
            Log.i(TAG, "onHandleIntent: Got a new result " + resultId);
            Resources res = getResources();
            Intent i = PhotoGalleryActivity.newIntent(getApplicationContext());
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);

            Notification notification = new NotificationCompat.Builder(getApplicationContext())
                    .setTicker(res.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(res.getString(R.string.new_pictures_title))
                    .setContentText(res.getString(R.string.new_pictures_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();

            showBackgroundNotification(0, notification);
        }

        queryPreferences.setLastResultId(resultId);
    }

    public static Intent newIntent(Context context){
        return new Intent(context, PollService.class);
    }

    public static void setServiceAlarm(Context context, boolean isOn){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        if(isOn)
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), POLL_INTERVAL, pi);
        else{
            am.cancel(pi);
            pi.cancel();
        }
        //queryPreferences.setAlarmOn(context, isOn);
    }

    public static boolean isServiceAlarmOn(Context context){
        Intent i = PollService.newIntent(context);

        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);

        return pi != null;
    }

    /**
     *Проверяем, есть ли доступная сеть. Если такая имеется проверяем ее на наличие соединения с помощью
     *менеджера соединений, возвращаем истину, если условия выполняются.
     */
    private boolean isNetworkAvailableAndConnected(){
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isAvailable = cm.getActiveNetworkInfo() != null;
        boolean isConnected = isAvailable &&
                cm.getActiveNetworkInfo().isConnected();

        return isConnected;
    }

    private void showBackgroundNotification(int requestCode, Notification notification){
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
    }
}
