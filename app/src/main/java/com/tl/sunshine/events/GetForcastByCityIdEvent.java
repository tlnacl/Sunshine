package com.tl.sunshine.events;

import com.tl.sunshine.models.WeatherForecast;

/**
 * Created by tomtang on 18/05/15.
 */
public class GetForcastByCityIdEvent {
    public WeatherForecast result;

    public GetForcastByCityIdEvent(WeatherForecast result) {
        this.result = result;
    }
}
