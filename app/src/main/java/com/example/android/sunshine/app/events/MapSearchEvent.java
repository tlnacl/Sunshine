package com.example.android.sunshine.app.events;

import com.google.gson.JsonObject;

/**
 * Created by tlnacl on 12/01/15.
 */
public class MapSearchEvent {
    public JsonObject result;

    public MapSearchEvent(JsonObject weatherData){ this.result = weatherData;}
}
