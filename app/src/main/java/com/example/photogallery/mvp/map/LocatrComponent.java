package com.example.photogallery.mvp.map;

import dagger.Subcomponent;

/**
 * Created by viktor on 29.08.17.
 */

@LocatrScope
@Subcomponent(modules = LocatrModule.class)
public interface LocatrComponent {
    void inject(LocatrFragment locatrFragment);
}
