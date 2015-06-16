package com.example.android.sunshine.app.di.components;


import com.example.android.sunshine.app.di.ActivityScope;
import com.example.android.sunshine.app.di.modules.ActivityModule;
import com.example.android.sunshine.app.ui.BaseActivity;
import com.example.android.sunshine.app.ui.ErrorCallback;

import dagger.Component;

@ActivityScope
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {
//    void inject(AbsFragment fragment);

    BaseActivity activity();
    ErrorCallback errorCallback();
}
