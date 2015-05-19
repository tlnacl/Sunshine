package com.example.android.sunshine.app.data.sharedpreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.android.sunshine.app.CoreApplication;
import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.models.WeatherForecast;
import com.example.android.sunshine.app.utils.helper.GsonHelper;
import com.google.gson.Gson;

import java.util.Map;

/**
 * Created by tomtang on 18/05/15.
 */
public class SharedPreferenceHelper {
    private static final Context sContext = CoreApplication.getContext();
    private static final Gson gson = GsonHelper.getGson();
    public static final String WEATHER_FORECAST = "com.example.android.sunshine.app.weather.forecast";

    private static SharedPreferences preferedWeatherForecast = sContext.getSharedPreferences(WEATHER_FORECAST, Context.MODE_PRIVATE);

    //can be multiple in next version
    public static WeatherForecast getPreferedWeatherForecast() {
        Map<String, String> prefers = (Map<String, String>) preferedWeatherForecast.getAll();
        if (prefers != null && !prefers.isEmpty()) {
            return gson.fromJson(prefers.entrySet().iterator().next().getValue(), WeatherForecast.class);
        }
        return null;
    }

    public static void storePreferedWeatherForecast(WeatherForecast weatherForecast) {
        //now only one prefer city
        SharedPreferences.Editor editor = preferedWeatherForecast.edit();
        editor.clear();
        editor.putString(String.valueOf(weatherForecast.getLocation().getCityId()),gson.toJson(weatherForecast));
    }

    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }
}
