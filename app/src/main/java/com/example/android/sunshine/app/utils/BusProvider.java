package com.example.android.sunshine.app.utils;

import com.squareup.otto.Bus;

/**
 * Created by tlnacl on 12/01/15.
 */
public final class BusProvider {

    private static final Bus BUS  = new Bus();

    public static Bus getInstance(){
        return BUS;
    }

    private BusProvider(){}
}
