package com.diegolucaccini.theflowtt.dal.contracts;


import android.provider.BaseColumns;

/**
 * Created by Diego Lucaccini on 01/07/2017.
 */

public class LocationsContract {

    private LocationsContract() {
    }

    public static class Table implements BaseColumns {

        public static final String NAME = "locations";
        public static final String COL_JOURNEY_ID = "journeyId";
        public static final String COL_ACCURACY = "accuracy";
        public static final String COL_LATITUDE = "latitude";
        public static final String COL_LONGITUDE = "longitude";
        public static final String COL_ALTITUDE = "altitude";
        public static final String COL_SPEED = "speed";

    }

}