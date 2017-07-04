package com.example.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Данный класс является автономным приемником (зарегестрирован в манифесте). Получает сообщение от системы
 * при загрузке устройства (BOOT_COMPLETED)
 * onReceive метод выполняет установку флага проверки наличия новых фото на ресурсе (setServiceAlarm)
 */
public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());

        Boolean isOn = QueryPreferences.isAlarmOn(context);
        PollService.setServiceAlarm(context, isOn);
    }
}
