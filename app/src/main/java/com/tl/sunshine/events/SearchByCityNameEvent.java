package com.tl.sunshine.events;

import com.tl.sunshine.models.CurrentWeather;

import java.util.List;

/**
 * Created by tomtang on 7/05/15.
 */
public class SearchByCityNameEvent {
    public List<CurrentWeather> result;

    public SearchByCityNameEvent(List<CurrentWeather> result) {
        this.result = result;
    }
}
