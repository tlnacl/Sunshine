package com.example.android.sunshine.app.events;

import com.example.android.sunshine.app.models.CurrentWeather;
import com.example.android.sunshine.app.models.WeatherForecast;

import java.util.List;

/**
 * Created by tomtang on 18/05/15.
 */
public class GetForcastByCityIdEvent {
    public WeatherForecast result;

    public GetForcastByCityIdEvent(WeatherForecast result) {
        this.result = result;
    }
}
