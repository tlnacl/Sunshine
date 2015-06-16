package com.example.android.sunshine.app.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.sunshine.app.network.NetworkException;

/**
 * Created by tomtang on 16/06/15.
 */
public interface ErrorCallback {
    void onApiError(@NonNull NetworkException exception);
    void onError(@Nullable String title, @NonNull String message);
}
