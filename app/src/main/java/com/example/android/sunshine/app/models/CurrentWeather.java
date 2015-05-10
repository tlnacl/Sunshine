package com.example.android.sunshine.app.models;

/**
 * Created by tomtang on 5/05/15.
 */
public class CurrentWeather extends WeatherBrief{
    private int cityId;
    private String cityName;
    private String country;
    private float latitude;
    private float longitude;
    private long timestamp;


    public CurrentWeather(int cityId, String cityName, String country, float latitude, float longitude, float temp, float high, float low, int weatherId, long timestamp) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.temp = temp;
        this.high = high;
        this.low = low;
        this.weatherId = weatherId;
        this.timestamp = timestamp;
    }

    public int getCityId() {
        return cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountry() {
        return country;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
