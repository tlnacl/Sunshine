package com.tl.sunshine.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import com.tl.sunshine.R;

import java.util.List;

/**
 * Created by tlnacl on 23/12/14.
 */
public abstract class BaseArFragment  extends BaseLocationFragment
        implements SensorEventListener {

    private static final int SENSOR_SPEED = SensorManager.SENSOR_DELAY_GAME;

    private SensorManager mSensorMgr;
    private Sensor mSensorGrav;
    private Sensor mSensorMag;
    private Sensor mSensorRotation;
    private GeomagneticField mMagneticField;

    private static final float temp[] = new float[16]; // Temporary rotation
    private static final float orientation[] = new float[3]; // Temporary rotation
    private static final float grav[] = new float[3];
    private static final float magn[] = new float[3];
    private float[] mRotationMatrix = new float[16];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize the rotation matrix to identity
        mRotationMatrix[0] = 1;
        mRotationMatrix[4] = 1;
        mRotationMatrix[8] = 1;
        mRotationMatrix[12] = 1;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mSensorMgr = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> sensors = mSensorMgr.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
        if (sensors.size() > 0) {
            mSensorRotation = sensors.get(0);
            mSensorMgr.registerListener(this, mSensorRotation, SENSOR_SPEED);
        }

        sensors = mSensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            mSensorGrav = sensors.get(0);
            mSensorMgr.registerListener(this, mSensorGrav, SENSOR_SPEED);
        }

        sensors = mSensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        if (sensors.size() > 0) {
            mSensorMag = sensors.get(0);
            mSensorMgr.registerListener(this, mSensorMag, SENSOR_SPEED);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            try {
                mSensorMgr.unregisterListener(this, mSensorGrav);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                mSensorMgr.unregisterListener(this, mSensorMag);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            mSensorMgr = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);

        mMagneticField = new GeomagneticField((float) location.getLatitude(),
                (float) location.getLongitude(),
                (float) location.getAltitude(), System.currentTimeMillis());

        updateUI(location);
    }

    @Override
    public void onStart() {
        super.onStart();

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.location_disabled_title))
                    .setMessage(getString(R.string.location_disabled_text))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.enable, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    }).create().show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) return;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            rootMeanSquareBuffer(grav, event.values);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            rootMeanSquareBuffer(magn, event.values);
        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            final int valuesLength = event.values.length;
            if (valuesLength == 3 || valuesLength == 4) {
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
            } else {
                if (valuesLength < 3) return; // Something wrong with firmware on the phone
                final float[] tempValues = new float[valuesLength < 4 ? 3 : 4];
                for (int i = 0; i < tempValues.length; ++i) {
                    tempValues[i] = event.values[i];
                }
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, tempValues);
            }
            updateRotationMatrix(mRotationMatrix);
            return;
        }

        SensorManager.getRotationMatrix(temp, null, grav, magn);
        if (mSensorRotation == null) {
            float[] tempRotation = temp.clone();
            try {
                updateRotationMatrix(tempRotation);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_X, SensorManager.AXIS_Z, temp);
        SensorManager.getOrientation(temp, orientation);

        updateOrientation(orientation[0], orientation[1], orientation[2],
                mMagneticField != null ? mMagneticField.getDeclination() : 0);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void rootMeanSquareBuffer(float[] target, float[] values) {
        final float amplification = 100.0f;
        float buffer = 10.0f;

        target[0] += amplification;
        target[1] += amplification;
        target[2] += amplification;
        values[0] += amplification;
        values[1] += amplification;
        values[2] += amplification;

        target[0] = (float) (Math
                .sqrt((target[0] * target[0] * buffer + values[0] * values[0])
                        / (1 + buffer)));
        target[1] = (float) (Math
                .sqrt((target[1] * target[1] * buffer + values[1] * values[1])
                        / (1 + buffer)));
        target[2] = (float) (Math
                .sqrt((target[2] * target[2] * buffer + values[2] * values[2])
                        / (1 + buffer)));

        target[0] -= amplification;
        target[1] -= amplification;
        target[2] -= amplification;
        values[0] -= amplification;
        values[1] -= amplification;
        values[2] -= amplification;
    }

    abstract public void updateUI(Location location);

    abstract public void updateRotationMatrix(float[] rotationMatrix);

    abstract public void updateOrientation(float x, float y, float z, float m);
}
