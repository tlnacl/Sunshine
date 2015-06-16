package com.example.android.sunshine.app.models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tomtang on 6/05/15.
 */
public class WeatherForecast  implements Serializable {
    private Location location;
    private List<WeatherDetail> weather;

    public WeatherForecast(Location location, List<WeatherDetail> weather) {
        this.location = location;
        this.weather = weather;
    }

    public Location getLocation() {
        return location;
    }

    public List<WeatherDetail> getWeather() {
        return weather;
    }
}
