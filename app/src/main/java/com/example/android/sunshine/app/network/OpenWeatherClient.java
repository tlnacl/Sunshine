package com.example.android.sunshine.app.network;

import com.example.android.sunshine.app.events.MapSearchEvent;
import com.example.android.sunshine.app.models.CurrentWeather;
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

        RetrofitHelper.getServerApi().weatherMapSearch(options, new Callback<CityInCycleEnvelope>() {
            @Override
            public void success(CityInCycleEnvelope weatherDataEnvelope, Response response) {
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

    //do it sync
    public WeatherForecast getForcastByCity(int cityId){
        return OpenWeatherDataParse.parseDailyWeather(RetrofitHelper.getServerApi().getForcastByCity(cityId));
    }

    @Produce
    public MapSearchEvent produceMapSearchEvent(List<CurrentWeather> currentWeathers) {
        return new MapSearchEvent(currentWeathers);
    }

    protected class DailyWeatherEnvelop extends WeatherDataEnvelope {
        public City city;
        @SerializedName("list")
        public ArrayList<ForcastDataEnvelope> weatherDataEnvelopes;

        class City {
            @SerializedName("id")
            public int cityId;
            @SerializedName("name")
            public String cityName;
            public Coord coord;
        }
    }

    protected class CityInCycleEnvelope extends WeatherDataEnvelope {
        @SerializedName("list")
        public ArrayList<CurrentWeatherDataEnvelope> weatherDataEnvelopes;
    }

    protected class ForcastDataEnvelope {
        @SerializedName("dt")
        public long timestamp;
        @SerializedName("weather")
        public ArrayList<Weather> weathers;
        public Temp temp;
        public float pressure;
        public int humidity;
        @SerializedName("speed")
        public float windSpeed;
        @SerializedName("deg")
        public float windDirection;

        class Temp {
            @SerializedName("day")
            public float temp;
            @SerializedName("min")
            public float temp_min;
            @SerializedName("max")
            public float temp_max;
        }
    }

    protected class CurrentWeatherDataEnvelope {
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
    }

    /**
     * Base class for results returned by the weather web service.
     */
    protected class WeatherDataEnvelope {
        @SerializedName("cod")
        private int httpCode;
    }

    protected class Weather {
        public int weatherId;
        public String description;
    }

    class Coord {
        public float lon;
        public float lat;
    }
}
