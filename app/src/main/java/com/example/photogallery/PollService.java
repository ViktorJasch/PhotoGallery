package com.example.photogallery;

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

import com.example.photogallery.model.GalleryItem;
import com.example.photogallery.network.FlickrFetch;
import com.example.photogallery.photos.PhotoGalleryActivity;

import java.util.List;

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


    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //необходимо проверить сеть на существование и доступность (ибо с чем еще работать?)
        if(!isNetworkAvailableAndConnected())
            return;

        String query = QueryPreferences.getStoredQuery(this);
        String lastResultId = QueryPreferences.getLastResultId(this);
        List<GalleryItem> items;

        if(query == null)
            items = new FlickrFetch().fetchRecentPhoto();
        else
            items = new FlickrFetch().searchPhoto(query);

        if(items.size() == 0)
            return;

        String resultId = items.get(0).getId();
        if(resultId.equals(lastResultId))
            Log.i(TAG, "onHandleIntent: Got an old result " + resultId);
        else{
            Log.i(TAG, "onHandleIntent: Got a new result " + resultId);
            Resources res = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(res.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(res.getString(R.string.new_pictures_title))
                    .setContentText(res.getString(R.string.new_pictures_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();

            showBackgroundNotification(0, notification);
        }

        QueryPreferences.setLastResultId(this ,resultId);
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
        QueryPreferences.setAlarmOn(context, isOn);
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
