package com.example.photogallery.permissions;


import android.Manifest;
import android.content.res.Resources;

import com.example.photogallery.Constants;
import com.example.photogallery.R;

/**
 * Created by viktor on 29.08.17.
 */

public class FineLocationPermissionDefinition extends BasePermissionDefinition {
    @Override
    public String getPermission() {
        return Manifest.permission.ACCESS_FINE_LOCATION;
    }

    @Override
    public String getDescription() {
        return Resources.getSystem().getString(R.string.fine_location_explanation);
    }

    @Override
    public int getRequestCode() {
        return Constants.PERMISSION_ACCESS_FINE_LOCATION;
    }
}
