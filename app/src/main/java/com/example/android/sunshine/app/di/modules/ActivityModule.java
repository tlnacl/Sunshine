package com.example.android.sunshine.app.di.modules;

import com.example.android.sunshine.app.di.ActivityScope;
import com.example.android.sunshine.app.ui.BaseActivity;
import com.example.android.sunshine.app.ui.ErrorCallback;

import dagger.Module;
import dagger.Provides;

/**
 *
 * A module to wrap the Activity state and expose it to the graph.
 */
@Module
public final class ActivityModule {
    private final BaseActivity mActivity;

    public ActivityModule(BaseActivity activity) {
        mActivity = activity;
    }

    /**
     * Expose the activity to dependents in the graph.
     */
    @Provides
    @ActivityScope
    BaseActivity activity() {
        return mActivity;
    }

    @Provides
    @ActivityScope
    ErrorCallback errorCallback() {
        return mActivity;
    }
}
