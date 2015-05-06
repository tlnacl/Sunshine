package com.example.android.sunshine.app.models;

/**
 * Created by tomtang on 6/05/15.
 */
public class Location {
    private int cityId;
    private String cityName;
    private float lat;
    private float lon;

    public Location(int cityId, String cityName, float lat, float lon) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.lat = lat;
        this.lon = lon;
    }

    public int getCityId() {
        return cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
