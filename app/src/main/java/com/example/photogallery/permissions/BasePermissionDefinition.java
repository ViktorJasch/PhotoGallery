package com.example.photogallery.permissions;

import android.content.Context;
import android.content.res.Resources;

import com.example.photogallery.R;

/**
 * Created by viktor on 29.08.17.
 * После 6 версии андроид появилась возможность использовать "умный запрос" на permissions.
 * Такой запрос происходит в компоненте андроид (fragment, activity). Необходимо вынести информацию о конкретном
 * разрешении в отдельный класс, который предоставляет описательные методы для каждого конкретного разрешения.
 * Презентер будет передавать конкретный класс, наследующий BasePermissionDefinition, а активити (фрагмент),
 * вызывая описательные методы создавать AlertDialog и делать запрос.
 * При большом количестве разрешений, код в активити (фрагменте) меняться не будет
 */

public interface BasePermissionDefinition {
    //Получить само разрешение. Например Manifest.permission.ACCESS_FINE_LOCATION
    public String getPermission();
    //Получить описание разрешения. Помещается в AlertDialog для объяснения, зачем оно нужно
    public String getDescription();
    //Получить requestCode
    public int getRequestCode();
}
