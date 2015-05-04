package com.example.android.sunshine.app.network;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;

/**
 * Created by tlnacl on 16/12/14.
 */
public class RetrofitErrorHandler implements ErrorHandler {
    @Override
    public Throwable handleError(RetrofitError retrofitError) {
        retrofitError.printStackTrace();
        return retrofitError;
    }
}