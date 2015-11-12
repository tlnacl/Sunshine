package com.tl.sunshine.events;

import com.tl.sunshine.models.CurrentWeather;

import java.util.List;

/**
 * Created by tlnacl on 12/01/15.
 */
public class MapSearchEvent {
    public List<CurrentWeather> result;

    public MapSearchEvent(List<CurrentWeather> weatherData){ this.result = weatherData;}
}
