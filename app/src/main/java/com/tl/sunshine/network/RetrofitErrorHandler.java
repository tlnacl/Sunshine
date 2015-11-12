package com.tl.sunshine.network;

import android.content.Context;

import com.tl.sunshine.R;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import timber.log.Timber;

/**
 * Created by tlnacl on 16/12/14.
 */
public class RetrofitErrorHandler implements ErrorHandler {
    private final Context mContext;

    public RetrofitErrorHandler(Context ctx) {
        this.mContext = ctx;
    }

    @Override
    public Throwable handleError(RetrofitError cause) {
        String errorDescription;

        if (cause.getKind() == RetrofitError.Kind.NETWORK) {
            errorDescription = mContext.getString(R.string.error_network);
        } else {
            if (cause.getResponse() == null) {
                errorDescription = mContext.getString(R.string.error_no_response);
            } else {

                // Error message handling - return a simple error to Retrofit handlers..
                try {
                    ErrorResponse errorResponse = (ErrorResponse) cause.getBodyAs(ErrorResponse.class);
                    errorDescription = errorResponse.error.data.message;
                } catch (Exception ex) {
                    try {
                        errorDescription = mContext.getString(R.string.error_network_http_error, cause.getResponse().getStatus());
                    } catch (Exception ex2) {
                        Timber.e("handleError: " + ex2.getLocalizedMessage());
                        errorDescription = mContext.getString(R.string.error_unknown);
                    }
                }
            }
        }
        cause.printStackTrace();

        return new Exception(errorDescription);
    }
}