package com.tl.sunshine.utils;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by tlnacl on 12/01/15.
 */
public final class BusProvider {

    private static final Bus BUS  = new Bus(ThreadEnforcer.ANY);

    public static Bus getInstance(){
        return BUS;
    }

    private BusProvider(){}
}
