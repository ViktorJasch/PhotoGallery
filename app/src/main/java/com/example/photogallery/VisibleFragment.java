package com.example.photogallery;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.example.photogallery.mvp.photos.PollService;

/**
 * Класс необходим для регистрации динамического приемника широковещательных сообщений
 * (mOnShowNotification) при запуске приложения
 *
 */
public abstract class VisibleFragment extends Fragment {
    public static final String TAG = "VisibleFragment";

    @Override
    public void onStart() {
        super.onStart();

        //регистрация нового широковещательного приемника
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null);
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().unregisterReceiver(mOnShowNotification);
    }

    //Данный приемник срабатывает только когда приложение запущенно (так как он динамический).
    //Фильтром является ACTION_SHOW_NOTIFICATION. Сообщения с таким тегом вызываются при появлении новых фото
    //в классе PollService методом showBackgroundNotification.
    //Если этот применик существует (т.е. приложение запущенно), уведомление отменяется (что б не бесило),
    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i(TAG, "onReceive: canceling notification");
            setResultCode(Activity.RESULT_CANCELED);
        }
    };

}
