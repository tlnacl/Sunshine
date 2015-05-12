package com.example.android.sunshine.app;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;

/**
 * Created by tlnacl on 16/12/14.
 */
public class CoreApplication extends Application {
    private static CoreApplication instance;
    private static final String TAG = "APP";

    public CoreApplication(){
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
        if(BuildConfig.DEBUG) {
            Stetho.initialize(
                    Stetho.newInitializerBuilder(this)
                            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                            .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                            .build());
        }
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                .detectNetwork()
//                .penaltyLog()
//                .penaltyDialog()
//                .build());
//        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
//                .penaltyLog()
//                .build());
    }
}
