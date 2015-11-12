package com.tl.sunshine.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.tl.sunshine.BuildConfig;
import com.tl.sunshine.CoreApplication;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit.RequestInterceptor;
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
//            OkHttp 2.2 has a new feature called interceptors which would allow you to add a Cache-Control header despite the server not sending it. This is very dangerous but very powerful. Usually you would only do this in extreme cases.
            int cacheSize = 10 * 1024 * 1024; // 10 MiB
            File cacheDirectory = new File(CoreApplication.getContext().getCacheDir().getAbsolutePath(), "HttpCache");
            Cache cache = new Cache(cacheDirectory, cacheSize);
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(10, TimeUnit.SECONDS);
            client.setReadTimeout(10, TimeUnit.SECONDS);
            client.setCache(cache);
            client.interceptors().add(retryInterceptor);
            client.setRetryOnConnectionFailure(true);
            if(BuildConfig.DEBUG) {
                client.networkInterceptors().add(new StethoInterceptor());
            }
            Gson gson = new GsonBuilder().setDateFormat("ddMMyyyy HH:mm:ss").create();

            //excutor
            Executor httpExecutor = Executors.newCachedThreadPool();
            Executor callbackExecutor = Executors.newCachedThreadPool();

            RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(API_URL)
                    .setErrorHandler(new RetrofitErrorHandler(CoreApplication.getContext()))
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setConverter(new GsonConverter(gson))
                    .setClient(new OkClient(client))
                    .setRequestInterceptor(cacheInterceptor)
//                    .setExecutors(httpExecutor,callbackExecutor)
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

    //offline cache
    //Response caching uses HTTP headers for all configuration. You can add request headers like Cache-Control: max-stale=3600 and OkHttp's
    // cache will honor them. Your webserver configures how long responses are cached with its own response headers, like Cache-Control: max-age=9600.
    // There are cache headers to force a cached response, force a network response, or force the network response to be validated with a conditional GET.
    private static RequestInterceptor cacheInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            request.addHeader("Accept", "application/json;versions=1");
            if (isNetworkAvaliable()) {
                int maxAge = 60; // read from cache for 1 minute
                request.addHeader("Cache-Control", "public, max-age=" + maxAge);
            } else {
            int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
            request.addHeader("Cache-Control",
                    "public, only-if-cached, max-stale=" + maxStale);
            }
        }
    };

    public static boolean isNetworkAvaliable(){
        ConnectivityManager cm =
                (ConnectivityManager)CoreApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

}
