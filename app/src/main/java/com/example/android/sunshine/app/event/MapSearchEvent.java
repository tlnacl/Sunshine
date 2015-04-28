package com.example.android.sunshine.app.event;

import com.example.android.sunshine.app.ui.WeatherUI;

import java.util.List;

/**
 * Created by tlnacl on 12/01/15.
 */
public class MapSearchEvent {
    public List<WeatherUI> result;

    public MapSearchEvent(List<WeatherUI> weatherUI){ this.result = weatherUI;}
}
