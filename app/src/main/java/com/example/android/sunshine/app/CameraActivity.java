package com.example.android.sunshine.app;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.example.android.sunshine.app.ui.WeatherUI;
import com.example.android.sunshine.app.ui.WeatherViewObject;
import com.example.android.sunshine.app.ui.widgets.ArGlSurfaceView;
import com.example.android.sunshine.app.ui.widgets.CameraSurfaceView;

/**
 * Created by tlnacl on 18/12/14.
 */
public class CameraActivity extends BaseActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);
        getFragmentManager().beginTransaction().add(R.id.root_container,new CameraFragment(),"cameraView").commit();

    }
}
