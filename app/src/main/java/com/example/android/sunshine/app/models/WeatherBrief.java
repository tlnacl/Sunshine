package com.example.android.sunshine.app.models;

/**
 * Created by tomtang on 6/05/15.
 */
public class WeatherBrief {
    float temp;
    float high;
    float low;
    int weatherId;

    public WeatherBrief() {

    }

    public WeatherBrief(float temp, float high, float low, int weatherId) {
        this.temp = temp;
        this.high = high;
        this.low = low;
        this.weatherId = weatherId;
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
