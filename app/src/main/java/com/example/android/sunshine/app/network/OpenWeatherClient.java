package com.example.android.sunshine.app.network;

import com.example.android.sunshine.app.events.MapSearchEvent;
import com.example.android.sunshine.app.utils.BusProvider;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;
import com.squareup.otto.Produce;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by tomtang on 4/05/15.
 */
public class OpenWeatherClient {

    public void doMapSearch(LatLng latLng){
        Map<String,String> options = new HashMap<>();
        options.put("mode", "json");
        options.put("units", "metric");
        options.put("cnt", "10");
        options.put("lat", String.valueOf(latLng.latitude));
        options.put("lon", String.valueOf(latLng.longitude));

        RetrofitHelper.getServerApi().weatherMapSearch(options, new Callback<JsonObject>() {
            @Override
            public void success(JsonObject jsonObject, Response response) {
                BusProvider.getInstance().post(produceMapSearchEvent(jsonObject));
            }

            @Override
            public void failure(RetrofitError error) {
                BusProvider.getInstance().post(produceMapSearchEvent(null));
            }
        });
    }

    @Produce
    public MapSearchEvent produceMapSearchEvent(JsonObject jsonObject){
        return new MapSearchEvent(jsonObject);
    }
}
