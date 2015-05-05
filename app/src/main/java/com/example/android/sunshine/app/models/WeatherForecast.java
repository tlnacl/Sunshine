package com.example.android.sunshine.app.models;

/**
 * Created by tomtang on 5/05/15.
 */
public class WeatherForecast {
    private int cityId;
    private String cityName;
    private double latitude;
    private double longitude;
    private double temp;
    private double high;
    private double low;
    private int weatherId;

    public WeatherForecast(int cityId, String cityName, double latitude, double longitude, double temp, double high, double low, int weatherId) {
        this.cityId = cityId;
        this.cityName = cityName;
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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getTemp() {
        return temp;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public int getWeatherId() {
        return weatherId;
    }
}
