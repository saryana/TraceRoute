package com.gps.capstone.traceroute;

import com.squareup.otto.Bus;

/**
 * Created by saryana on 4/12/15.
 *
 * Bus provider for the application
 */
public class BusProvider {

    // Singleton bus to be used throughout the application
    private static Bus mInstance;

    /**
     * @return Gets the single instance of the bus
     */
    public static Bus getInstance() {
        if (mInstance == null) {
            mInstance = new Bus();
        }
        return mInstance;
    }

}
