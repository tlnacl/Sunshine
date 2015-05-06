package com.example.android.sunshine.app.network;

import com.google.gson.JsonObject;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;
import retrofit.http.QueryMap;

/**
 * Created by tlnacl on 15/01/15.
 */
public interface OpenWeatherService {
    //http://api.openweathermap.org/data/2.5/weather?id=2172797
//    final String WEATHER_CURRENT_BY_CITYID = WEATHER_BASE_URL+"weather";

    //http://api.openweathermap.org/data/2.5/forecast/daily?id=2193733&mode=json&units=metric&cnt=14
//    final String WEATHER_FORCAST_BY_CITYID = WEATHER_BASE_URL+"daily";

    //http://api.openweathermap.org/data/2.5/find?lat=55.5&lon=37.5&mode=json&units=metric&cnt=10
//    final String WEATHER_MAP_SEARCH = WEATHER_BASE_URL+"find";

    @GET("/forecast/daily?mode=json&units=metric&cnt=14")
    OpenWeatherClient.DailyWeatherEnvelop getForcastByCity(@Query("id") int cityId);

    @GET("/forecast/daily?mode=json&units=metric&cnt=14")
    void getForcastByCity(@Query("id") int cityId, Callback<OpenWeatherClient.DailyWeatherEnvelop> callback);

    @GET("/weather")
    JsonObject currentWeatherByCity(@Query("id") int cityId);

    @GET("/weather")
    void currentWeatherByCity(@Query("id") int cityId, Callback<JsonObject> callback);

    @GET("/find")//http://api.openweathermap.org/data/2.5/find?units=metric&lon=174.76877510547638&lat=-36.84631152204655&mode=json&cnt=10
    OpenWeatherClient.CityInCycleEnvelope weatherMapSearch(@QueryMap Map<String, String> options);

    @GET("/find")//http://api.openweathermap.org/data/2.5/find?units=metric&lon=174.76877510547638&lat=-36.84631152204655&mode=json&cnt=10
    void weatherMapSearch(@QueryMap Map<String, String> options, Callback<OpenWeatherClient.CityInCycleEnvelope> callback);

}
