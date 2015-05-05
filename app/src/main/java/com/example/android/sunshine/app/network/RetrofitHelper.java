package com.example.android.sunshine.app.network;

import com.example.android.sunshine.app.BuildConfig;
import com.example.android.sunshine.app.CoreApplication;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by tlnacl on 15/01/15.
 */
public final class RetrofitHelper {
    private static OpenWeatherService sOpenWeatherService;
    private static final String API_URL = BuildConfig.API_ENDPOINT;

    public RetrofitHelper() {
    }

    public static OpenWeatherService getServerApi() {
        if (sOpenWeatherService == null) {
            int cacheSize = 10 * 1024 * 1024; // 10 MiB
            File cacheDirectory = new File(CoreApplication.getContext().getCacheDir().getAbsolutePath(), "HttpCache");
            Cache cache = new Cache(cacheDirectory, cacheSize);
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(10, TimeUnit.SECONDS);
            client.setReadTimeout(10, TimeUnit.SECONDS);
            client.setCache(cache);
            client.interceptors().add(retryInterceptor);
            client.setRetryOnConnectionFailure(true);
            Gson gson = new GsonBuilder().setDateFormat("ddMMyyyy HH:mm:ss").create();
            RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(API_URL)
                    .setErrorHandler(new RetrofitErrorHandler())
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setConverter(new GsonConverter(gson))
                    .setClient(new OkClient(client))
                    .build();
            sOpenWeatherService = restAdapter.create(OpenWeatherService.class);
        }
        return sOpenWeatherService;
    }


    private static Interceptor retryInterceptor = new Interceptor() {
        @Override
        public com.squareup.okhttp.Response intercept(Interceptor.Chain chain) throws IOException {

            com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
            if (originalResponse.code() != HttpURLConnection.HTTP_OK) {
                //TODO retry After parsing the result, just call chain.proceed() again with the same, updated, or completely new request.
//                chain.request()
            }
            return originalResponse;
        }
    };

}
