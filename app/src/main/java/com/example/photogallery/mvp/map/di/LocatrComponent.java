package com.example.photogallery.mvp.map.di;

import com.example.photogallery.mvp.map.LocatrFragment;
import com.example.photogallery.mvp.map.LocatrPresenter;

import dagger.Subcomponent;

/**
 * Created by viktor on 29.08.17.
 */

@LocatrScope
@Subcomponent(modules = LocatrModule.class)
public interface LocatrComponent {
    void inject(LocatrFragment locatrFragment);
    LocatrPresenter presenter();
}
