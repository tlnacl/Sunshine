package com.example.android.sunshine.app.network;

import com.example.android.sunshine.app.models.CurrentWeather;
import com.example.android.sunshine.app.models.WeatherForecast;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RetrofitError;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by tomtang on 4/05/15.
 */
public final class OpenWeatherClient {

    public static Observable<List<CurrentWeather>> doMapSearch(LatLng latLng) {
        Map<String, String> options = new HashMap<>();
        options.put("mode", "json");
        options.put("units", "metric");
        options.put("cnt", "10");
        options.put("lat", String.format("%.4f", latLng.latitude));
        options.put("lon", String.format("%.4f", latLng.longitude));

        return RetrofitHelper.getServerApi().weatherMapSearch(options).map(new Func1<FindApiEnvelope, List<CurrentWeather>>() {
            @Override
            public List<CurrentWeather> call(FindApiEnvelope findApiEnvelope) {
                return OpenWeatherDataParse.parseCurrentWeathers(findApiEnvelope);
            }
        });

    }

    public static Observable<List<CurrentWeather>> doCityWeatherSearch(String cityName) {
        Map<String, String> options = new HashMap<>();
        options.put("mode", "json");
        options.put("units", "metric");
        options.put("cnt", "10");
        options.put("type", "like");
        options.put("q",cityName+"*");

        return RetrofitHelper.getServerApi().weatherMapSearch(options).map(new Func1<FindApiEnvelope, List<CurrentWeather>>() {
            @Override
            public List<CurrentWeather> call(FindApiEnvelope findApiEnvelope) {
                return OpenWeatherDataParse.parseCurrentWeathers(findApiEnvelope);
            }
        });
    }

    public static Observable<WeatherForecast> getForcastByCityAsync(int cityId){
        return RetrofitHelper.getServerApi().getForcastByCity(cityId).map(new Func1<DailyWeatherEnvelop, WeatherForecast>() {
            @Override
            public WeatherForecast call(DailyWeatherEnvelop dailyWeatherEnvelop) {
                return OpenWeatherDataParse.parseDailyWeather(dailyWeatherEnvelop);
            }
        });
    }

    //do it sync
    public WeatherForecast getForcastByCityInSync(int cityId){
        WeatherForecast weatherForecast = null;
        try {
            weatherForecast = OpenWeatherDataParse.parseDailyWeather(RetrofitHelper.getServerApi().getForcastByCitySync(cityId));
        } catch (RetrofitError e){

        }
        return weatherForecast;
    }

    public class DailyWeatherEnvelop extends WeatherDataEnvelope {
        public City city;
        @SerializedName("list")
        public ArrayList<ForcastDataEnvelope> weatherDataEnvelopes;

        public class City {
            @SerializedName("id")
            public int cityId;
            @SerializedName("name")
            public String cityName;
            public Coord coord;
        }
    }

    public class FindApiEnvelope extends WeatherDataEnvelope {
        @SerializedName("list")
        public ArrayList<CurrentWeatherDataEnvelope> weatherDataEnvelopes;
    }

    public class ForcastDataEnvelope {
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

        public class Temp {
            @SerializedName("day")
            public float temp;
            @SerializedName("min")
            public float temp_min;
            @SerializedName("max")
            public float temp_max;
        }
    }

    public class CurrentWeatherDataEnvelope {
        @SerializedName("id")
        public int cityId;
        @SerializedName("name")
        public String cityName;
        @SerializedName("dt")
        public long timestamp;
        public Main main;
        public Sys sys;
        public Coord coord;
        @SerializedName("weather")
        public ArrayList<Weather> weathers;

        public class Main {
            public float temp;
            public float temp_min;
            public float temp_max;
            public float pressure;
            public int humidity;
        }

        public class Sys{
            public String country;
        }
    }

    /**
     * Base class for results returned by the weather web service.
     */
    public class WeatherDataEnvelope {
        @SerializedName("cod")
        public int httpCode;

        public WeatherDataEnvelope filterResponse(){
            if(httpCode == 200){
                return this;
            } else {
                return null;
            }
        }
    }

    public class Weather {
        public int weatherId;
        public String description;
    }

    public class Coord {
        public float lon;
        public float lat;
    }
}
