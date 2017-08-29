package com.example.photogallery.service;

import dagger.Component;
import dagger.Subcomponent;

/**
 * Created by viktor on 29.08.17.
 */

@PollServiceScope
@Subcomponent(modules = PollServiceModule.class)
public interface PollServiceComponent {
    void inject(PollService service);
}
