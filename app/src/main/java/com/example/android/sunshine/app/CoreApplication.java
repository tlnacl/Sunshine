package com.example.android.sunshine.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.facebook.stetho.Stetho;

import timber.log.Timber;

/**
 * Created by tomtang on 21/05/15.
 */
public class CoreApplication extends Application {
    private static CoreApplication instance;

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
            Timber.plant(new Timber.DebugTree());
            Stetho.initialize(
                    Stetho.newInitializerBuilder(this)
                            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                            .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                            .build());
        }else{
//            Timber.plant(new CrashReportingTree());
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

    /** A tree which logs important information for crash reporting. */
    private static class CrashReportingTree extends Timber.Tree {
        @Override protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }

            FakeCrashLibrary.log(priority, tag, message);

            if (t != null) {
                if (priority == Log.ERROR) {
                    FakeCrashLibrary.logError(t);
                } else if (priority == Log.WARN) {
                    FakeCrashLibrary.logWarning(t);
                }
            }
        }
    }
}
