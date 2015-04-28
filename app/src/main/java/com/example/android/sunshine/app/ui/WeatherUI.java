package com.example.android.sunshine.app.ui;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.example.android.sunshine.app.utils.ParcelableModel;

/**
 * Created by tlnacl on 17/12/14.
 */
public class WeatherUI extends ParcelableModel implements ClusterItem, Cloneable {
    public static final Creator<WeatherUI> CREATOR = new ParcelableCreator<WeatherUI>(WeatherUI.class);

//    public WeatherUI(double latitude, double longitude) {
//        this.latitude = latitude;
//        this.longitude = longitude;
//    }
    private int cityId;
    private String cityName;
    private double latitude;
    private double longitude;
    private double temp;
    private double high;
    private double low;
    private long parcelID;
    private int weatherId;

    private String description;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getParcelID() {
        return parcelID;
    }

    public void setParcelID(long parcelID) {
        this.parcelID = parcelID;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public String toString() {
        return "WeatherUI{" +
                "cityName='" + cityName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WeatherUI weatherUI = (WeatherUI) o;

        if (Double.compare(weatherUI.latitude, latitude) != 0) return false;
        if (Double.compare(weatherUI.longitude, longitude) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public int getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(int weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
