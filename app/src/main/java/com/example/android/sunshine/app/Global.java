package com.example.android.sunshine.app;

/**
 * Created by tlnacl on 21/01/15.
 */
public class Global {
    private static int selectedCityId = -1;


    public static int getSelectedCityId() {
        return selectedCityId;
    }

    public static void setSelectedCityId(int selectedCityId) {
        Global.selectedCityId = selectedCityId;
    }
}
