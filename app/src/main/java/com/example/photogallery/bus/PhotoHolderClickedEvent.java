package com.example.photogallery.bus;

import android.net.Uri;

/**
 * Created by viktor on 02.07.17.
 */

public class PhotoHolderClickedEvent {
    Uri photoUri;

    public PhotoHolderClickedEvent(Uri uri){
        photoUri = uri;
    }

    public Uri getPhotoUri() {
        return photoUri;
    }
}
