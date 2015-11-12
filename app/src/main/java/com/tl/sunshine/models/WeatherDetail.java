package com.tl.sunshine.models;

/**
 * Created by tomtang on 6/05/15.
 */
public class WeatherDetail extends WeatherBrief {
    private float humidity;
    private float pressure;
    private float windSpeed;
    private float windDirection;
    private String description;
    private long timestamp;

    public WeatherDetail(float temp, float high, float low, int weatherId, float humidity, float pressure, float windSpeed, float windDirection, String description, long timestamp) {
        this.humidity = humidity;
        this.pressure = pressure;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.description = description;
        this.temp = temp;
        this.high = high;
        this.low = low;
        this.weatherId = weatherId;
        this.timestamp = timestamp;
    }


    public float getHumidity() {
        return humidity;
    }

    public float getPressure() {
        return pressure;
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    public float getWindDirection() {
        return windDirection;
    }

    public String getDescription() {
        return description;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
