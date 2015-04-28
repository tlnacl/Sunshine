package com.example.android.sunshine.app;

import android.app.Application;
import android.content.Context;

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

    }
}
