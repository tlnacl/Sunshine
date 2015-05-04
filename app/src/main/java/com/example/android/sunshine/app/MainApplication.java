package com.example.android.sunshine.app;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

/**
 * Created by tlnacl on 16/12/14.
 */
public class MainApplication extends Application {
    private static MainApplication instance;
    private static final String TAG = "APP";

    public MainApplication(){
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }

    // ***************************************
    // Application Methods
    // ***************************************
    @Override
    public void onCreate() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDialog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
                .penaltyLog()
                .build());
    }
}
