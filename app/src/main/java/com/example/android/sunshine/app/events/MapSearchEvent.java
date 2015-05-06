package com.example.android.sunshine.app.events;

import com.example.android.sunshine.app.models.CurrentWeather;

import java.util.List;

/**
 * Created by tlnacl on 12/01/15.
 */
public class MapSearchEvent {
    public List<CurrentWeather> result;

    public MapSearchEvent(List<CurrentWeather> weatherData){ this.result = weatherData;}
}
