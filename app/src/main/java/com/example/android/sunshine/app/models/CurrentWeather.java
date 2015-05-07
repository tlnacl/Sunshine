package com.example.android.sunshine.app.models;

/**
 * Created by tomtang on 5/05/15.
 */
public class CurrentWeather extends WeatherBrief{
    private int cityId;
    private String cityName;
    private String country;
    private double latitude;
    private double longitude;


    public CurrentWeather(int cityId, String cityName, String country, double latitude, double longitude, float temp, float high, float low, int weatherId) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.temp = temp;
        this.high = high;
        this.low = low;
        this.weatherId = weatherId;
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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
