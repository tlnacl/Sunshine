package com.example.android.sunshine.app.di.components;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.android.sunshine.app.di.modules.ApplicationModule;
import com.example.android.sunshine.app.di.modules.OpenWeatherServiceModule;
import com.example.android.sunshine.app.network.OpenWeatherService;
import com.example.android.sunshine.app.ui.BaseActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        ApplicationModule.class,
        OpenWeatherServiceModule.class
})
public interface ApplicationComponent {
    void inject(BaseActivity activity);

    //Exposed to sub-graphs.
    Context context();
    OpenWeatherService openWeatherService();
    SharedPreferences preferences();
}
