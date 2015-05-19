package com.example.android.sunshine.app.ui;

import android.app.Fragment;

import com.example.android.sunshine.app.utils.BusProvider;
import com.squareup.otto.Bus;

/**
 * Created by tomtang on 19/05/15.
 */
public class BaseFragment extends Fragment {

    @Override
    public void onResume() {
        super.onResume();

        // Register ourselves so that we can provide the initial value.
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Always unregister when an object no longer should be on the bus.
        BusProvider.getInstance().unregister(this);
    }
}
