package com.example.android.sunshine.app.ui;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.ui.BaseArFragment;
import com.example.android.sunshine.app.ui.WeatherUI;
import com.example.android.sunshine.app.ui.WeatherViewObject;
import com.example.android.sunshine.app.ui.widgets.ArGlSurfaceView;
import com.example.android.sunshine.app.ui.widgets.CameraSurfaceView;

/**
 * Created by tlnacl on 23/12/14.
 */
public class CameraFragment extends BaseArFragment implements WeatherViewObject.ItemSelectionListener{

    private CameraSurfaceView mCameraView;
    private ArGlSurfaceView mWeatherGlView;
    private Location mLocation;

    private FrameLayout mRootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (FrameLayout) inflater.inflate(R.layout.fragment_camera, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCameraView = (CameraSurfaceView) mRootView.findViewById(R.id.camera);
        mCameraView.setZOrderMediaOverlay(true);

        mWeatherGlView = (ArGlSurfaceView) mRootView.findViewById(R.id.ar_view);
        mWeatherGlView.setZOrderOnTop(true);
        mWeatherGlView.setItemSelectionListener(this);

    }

    @Override
    public void onSelection(WeatherUI... weatherUIs) {
        Toast.makeText(getActivity(), "on weatherUI select", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public void updateUI(Location location) {
        if(mLocation!=null) {
            if(mLocation.distanceTo(location) < 25f) {
                return;
            }
        }
        mLocation = location;
    }

    @Override
    public void updateRotationMatrix(float[] rotationMatrix) {
        mWeatherGlView.setRotationMatrix(rotationMatrix);
    }

    @Override
    public void updateOrientation(float x, float y, float z, float m) {
        mWeatherGlView.setRotation(x, y, z, m);
    }
}
