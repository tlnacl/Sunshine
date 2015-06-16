package com.example.android.sunshine.app.di.modules;

import android.content.SharedPreferences;

import com.example.android.sunshine.app.network.OpenWeatherService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class OpenWeatherServiceModule {

    @Singleton
    @Provides
    OpenWeatherService provideOpenWeatherServiceAdapter(SharedPreferences preferences) {
        return getAdapter(preferences);
    }

    protected OpenWeatherService getAdapter(SharedPreferences preferences) {
        throw new UnsupportedOperationException("getAdapter() not implemented");
    }
}
