package com.example.android.sunshine.app.network;

import com.example.android.sunshine.app.ui.WeatherUI;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tlnacl on 12/01/15.
 */
public class RetrofitWeatherData {

    public static final String LOG_TAG = RetrofitWeatherData.class.getSimpleName();

    public static List<WeatherUI> parseWeatherData(JsonObject forecastJson) {
        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.
        List<WeatherUI> weathers = new ArrayList<>();


        // Location information
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";

        // Location coordinate
        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";

        // Weather information.  Each day's forecast info is an element of the "list" array.
        final String OWM_LIST = "list";

        final String OWM_DATETIME = "dt";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WIND = "wind";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        final String OWM_MAIN = "main";
        final String OWM_MAX = "temp_max";
        final String OWM_MIN = "temp_min";
        final String OWM_TEMP = "temp";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_CITY_ID = "id";

            JsonArray cityArray = forecastJson.getAsJsonArray(OWM_LIST);

            for(int i = 0; i < cityArray.size(); i++) {
                // These are the values that will be collected.

                JsonObject weatherJson = cityArray.get(i).getAsJsonObject();
                String cityName = weatherJson.get(OWM_CITY_NAME).getAsString();

                JsonObject cityCoord = weatherJson.getAsJsonObject(OWM_COORD);
                double cityLatitude = cityCoord.get(OWM_LATITUDE).getAsDouble();
                double cityLongitude = cityCoord.get(OWM_LONGITUDE).getAsDouble();

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime = weatherJson.get(OWM_DATETIME).getAsLong();
                int cityId = weatherJson.get(OWM_CITY_ID).getAsInt();

                JsonObject main = weatherJson.getAsJsonObject(OWM_MAIN);
                double pressure = main.get(OWM_PRESSURE).getAsDouble();
                int humidity = main.get(OWM_HUMIDITY).getAsInt();
                double high = main.get(OWM_MAX).getAsDouble();
                double low = main.get(OWM_MIN).getAsDouble();
                double temp = main.get(OWM_TEMP).getAsDouble();

                JsonObject wind = weatherJson.getAsJsonObject(OWM_WIND);
                double windSpeed = wind.get(OWM_WINDSPEED).getAsDouble();
                double windDirection = wind.get(OWM_WIND_DIRECTION).getAsDouble();

                // Description is in a child array called "weather", which is 1 element long.
                // That element also contains a weather code.
                JsonObject weatherObject =
                        weatherJson.getAsJsonArray(OWM_WEATHER).get(0).getAsJsonObject();
                String description = weatherObject.get(OWM_DESCRIPTION).getAsString();
                int weatherId = weatherObject.get(OWM_CITY_ID).getAsInt();

//            Weather weather = new Weather();
//            weather.setDateTime(dateTime);
//            weather.setDescription(description);
//            weather.setHigh(high);
//            weather.setHumidity(humidity);
//            weather.setLow(low);
//            weather.setPressure(pressure);
//            weather.setCityId(cityId);
//            weather.setCityName(cityName);
//            weather.setLatitude(cityLatitude);
//            weather.setLongitude(cityLongitude);
//            weather.setWindSpeed(windSpeed);
//            weather.setWindDirection(windDirection);
//
//            weathers.add(weather);

                WeatherUI weather= new WeatherUI();
                weather.setDescription(description);
                weather.setCityId(cityId);
                weather.setCityName(cityName);
                weather.setLatitude(cityLatitude);
                weather.setLongitude(cityLongitude);
                weather.setHigh(high);
                weather.setLow(low);
                weather.setWeatherId(weatherId);
                weathers.add(weather);
            }
//        Log.i(LOG_TAG, "weather list"+weathers);

        return weathers;
    }
}
