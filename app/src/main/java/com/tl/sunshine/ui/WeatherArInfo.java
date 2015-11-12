package com.tl.sunshine.ui;

import android.location.Location;
import android.support.annotation.NonNull;

/**
 * Created by tlnacl on 22/12/14.
 */
public class WeatherArInfo {

public static final float MAX_DISTANCE = 50;
private final WeatherUI weatherUI;
private final float[] mDistanceArray;
private final float mAzimuth;
private float mDepth = 1.0f;
private float[] mWindowPosition;

        public WeatherArInfo(@NonNull WeatherUI property, @NonNull Location location) {
            weatherUI = property;
            mDistanceArray = new float[3];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                    property.getLatitude(), property.getLongitude(),
                    mDistanceArray);

            mAzimuth = mDistanceArray[1];
        }

//        public long getId() {
//            return weatherUI.getQPID();
//        }

        public WeatherUI getWeatherUI() {
            return weatherUI;
        }

        public float getAzimuth() {
            return mAzimuth;
        }

        public float[] getDistanceArray() {
            return mDistanceArray;
        }

        public float getGlDistance() {
            float temp = 5f * mDistanceArray[0] / MAX_DISTANCE;
            float distance = (float) (10.0 * (Math.atan(temp * 2 - 9) + Math.PI / 2) / Math.PI);
            if (mDepth >= 0) {
                return distance * mDepth;
            } else {
                return mDepth;
            }
        }

        public void setWindowPosition(float[] windowPosition) {
            mWindowPosition = windowPosition;
        }

        public float[] getWindowPosition() {
            return mWindowPosition;
        }
}
