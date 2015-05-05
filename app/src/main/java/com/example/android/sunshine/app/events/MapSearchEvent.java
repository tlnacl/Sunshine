package com.example.android.sunshine.app.events;

import com.example.android.sunshine.app.models.WeatherForecast;

import java.util.List;

/**
 * Created by tlnacl on 12/01/15.
 */
public class MapSearchEvent {
    public List<WeatherForecast> result;

    public MapSearchEvent(List<WeatherForecast> weatherData){ this.result = weatherData;}
}
