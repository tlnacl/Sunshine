package com.example.android.sunshine.app.network;

import com.example.android.sunshine.app.models.CurrentWeather;
import com.example.android.sunshine.app.models.Location;
import com.example.android.sunshine.app.models.WeatherDetail;
import com.example.android.sunshine.app.models.WeatherForecast;
import com.example.android.sunshine.app.utils.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tomtang on 5/05/15.
 */
public final class OpenWeatherDataParse {
    public static CurrentWeather parseCurrentWeather(OpenWeatherClient.CurrentWeatherDataEnvelope envelope) {
        //int cityId, String cityName, double latitude, double longitude, double temp, double high, double low, int weatherId
        return new CurrentWeather(envelope.cityId, envelope.cityName, envelope.sys.country, envelope.coord.lat, envelope.coord.lon,
                envelope.main.temp, envelope.main.temp_max, envelope.main.temp_min, envelope.weathers.get(0).weatherId, getDbDateString(envelope.timestamp));
    }

    public static List<CurrentWeather> parseCurrentWeathers(OpenWeatherClient.FindApiEnvelope envelope) {
        List<CurrentWeather> currentWeathers = new ArrayList<>();
        for (OpenWeatherClient.CurrentWeatherDataEnvelope weather : envelope.weatherDataEnvelopes) {
            currentWeathers.add(parseCurrentWeather(weather));
        }
        return currentWeathers;
    }

    public static WeatherForecast parseDailyWeather(OpenWeatherClient.DailyWeatherEnvelop envelope) {
        //int cityId, String cityName, double latitude, double longitude, double temp, double high, double low, int weatherId
        Location location = new Location(envelope.city.cityId, envelope.city.cityName, envelope.city.coord.lat, envelope.city.coord.lon);
        List<WeatherDetail> weatherDetails = new ArrayList<>();
        for (OpenWeatherClient.ForcastDataEnvelope forcastDataEnvelope : envelope.weatherDataEnvelopes) {
            weatherDetails.add(new WeatherDetail(forcastDataEnvelope.temp.temp, forcastDataEnvelope.temp.temp_max,
                    forcastDataEnvelope.temp.temp_min, forcastDataEnvelope.weathers.get(0).weatherId,
                    forcastDataEnvelope.humidity, forcastDataEnvelope.pressure, forcastDataEnvelope.windSpeed,
                    forcastDataEnvelope.windDirection, forcastDataEnvelope.weathers.get(0).description, getDbDateString(forcastDataEnvelope.timestamp)));
        }
        return new WeatherForecast(location, weatherDetails);
    }

    private static String getDbDateString(long timestamp){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat sdf = new SimpleDateFormat(Utility.DATE_FORMAT);
        return sdf.format(new Date(timestamp * 1000L));
    }
}
