package com.example.android.sunshine.app.utils.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by tomtang on 19/05/15.
 */
public class GsonHelper {
    private static Gson sGson;

    public static Gson getGson() {
        if (sGson == null) {
            sGson = new GsonBuilder().setDateFormat("ddMMyyyy HH:mm:ss").create();
        }
        return sGson;
    }
}
