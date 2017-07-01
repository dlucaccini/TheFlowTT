package com.diegolucaccini.theflowtt.dal.contracts;

import android.provider.BaseColumns;

/**
 * Created by Diego Lucaccini on 01/07/2017.
 */

public final class JourneyContract {

    private JourneyContract() {
    }

    public static class Table implements BaseColumns {
        public static final String NAME = "journey";
        public static final String COL_JOURNEY_ID = "JOURNEY_ID";
        public static final String COL_START_JOURNEY = "START_JOURNEY";
        public static final String COL_END_JOURNEY = "END_JOURNEY";
    }
}
