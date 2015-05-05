package com.example.android.sunshine.app.network;

import com.example.android.sunshine.app.models.WeatherForecast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tomtang on 5/05/15.
 */
public final class OpenWeatherDataParse {
    public static WeatherForecast parseCurrentWeather(OpenWeatherClient.CurrentWeatherDataEnvelope envelope) {
        //int cityId, String cityName, double latitude, double longitude, double temp, double high, double low, int weatherId
        return new WeatherForecast(envelope.cityId, envelope.cityName, envelope.coord.lat, envelope.coord.lon,
                envelope.main.temp, envelope.main.temp_max, envelope.main.temp_min, envelope.weathers.get(0).weatherId);
    }

    public static List<WeatherForecast> parseCurrentWeathers(List<OpenWeatherClient.CurrentWeatherDataEnvelope> envelope) {
        List<WeatherForecast> weatherForecasts = new ArrayList<>();
        for (OpenWeatherClient.CurrentWeatherDataEnvelope weather : envelope) {
            weatherForecasts.add(parseCurrentWeather(weather));
        }
        return weatherForecasts;
    }
}
