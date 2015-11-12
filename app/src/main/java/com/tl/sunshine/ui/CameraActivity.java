package com.tl.sunshine.ui;

import android.os.Bundle;

import com.tl.sunshine.R;

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
