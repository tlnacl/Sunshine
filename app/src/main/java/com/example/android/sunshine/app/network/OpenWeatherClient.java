package com.example.android.sunshine.app.network;

import com.example.android.sunshine.app.events.MapSearchEvent;
import com.example.android.sunshine.app.models.WeatherForecast;
import com.example.android.sunshine.app.utils.BusProvider;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
import com.squareup.otto.Produce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by tomtang on 4/05/15.
 */
public final class OpenWeatherClient {

    public void doMapSearch(LatLng latLng) {
        Map<String, String> options = new HashMap<>();
        options.put("mode", "json");
        options.put("units", "metric");
        options.put("cnt", "10");
        options.put("lat", String.format("%.4f", latLng.latitude));
        options.put("lon", String.format("%.4f", latLng.longitude));

        RetrofitHelper.getServerApi().weatherMapSearch(options, new Callback<List<CurrentWeatherDataEnvelope>>() {
            @Override
            public void success(List<CurrentWeatherDataEnvelope> weatherDataEnvelope, Response response) {
                //parse to business object
                BusProvider.getInstance().post(produceMapSearchEvent(OpenWeatherDataParse.parseCurrentWeathers(weatherDataEnvelope)));
            }

            @Override
            public void failure(RetrofitError error) {
                //TODO post(new ApiErrorEvent(error));
                BusProvider.getInstance().post(produceMapSearchEvent(null));
            }
        });
    }

    @Produce
    public MapSearchEvent produceMapSearchEvent(List<WeatherForecast> weatherForecasts) {
        return new MapSearchEvent(weatherForecasts);
    }

    protected class CurrentWeatherDataEnvelope extends WeatherDataEnvelope{
        @SerializedName("id")
        public int cityId;
        @SerializedName("name")
        public String cityName;
        @SerializedName("dt")
        public long timestamp;
        public Main main;
        public Coord coord;
        @SerializedName("weather")
        public ArrayList<Weather> weathers;

        class Main {
            public float temp;
            public float temp_min;
            public float temp_max;
            public float pressure;
            public int humidity;
        }

        class Coord {
            public float lon;
            public float lat;
        }
    }

    /**
     * Base class for results returned by the weather web service.
     */
    protected class WeatherDataEnvelope {
        @SerializedName("cod")
        private int httpCode;

        class Weather {
            public int weatherId;
            public String description;
        }
    }
}
