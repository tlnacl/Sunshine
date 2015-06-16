package com.example.android.sunshine.app.di.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class PreferenceModule {

    private final Context mContext;

    public PreferenceModule(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    @Provides
    @Singleton
    SharedPreferences providePreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }
}
