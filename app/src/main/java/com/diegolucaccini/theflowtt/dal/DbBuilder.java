package com.diegolucaccini.theflowtt.dal;

import com.diegolucaccini.theflowtt.dal.contracts.JourneyContract;
import com.diegolucaccini.theflowtt.dal.contracts.LocationsContract;

/**
 * Created by Diego Lucaccini on 01/07/2017.
 */

public class DbBuilder {

    public static final String CREATE_JOURNEY_TABLE = "CREATE TABLE "
            + JourneyContract.Table.NAME
            + " ("
            + JourneyContract.Table._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + JourneyContract.Table.COL_JOURNEY_ID + " TEXT,"
            + JourneyContract.Table.COL_START_JOURNEY + " TEXT,"
            + JourneyContract.Table.COL_END_JOURNEY + " TEXT )";


    public static final String CREATE_LOCATIONS_TABLE = "CREATE TABLE "
            + LocationsContract.Table.NAME
            + " ("
            + LocationsContract.Table._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + LocationsContract.Table.COL_JOURNEY_ID + " INTEGER NOT NULL,"
            + LocationsContract.Table.COL_ACCURACY + " TEXT,"
            + LocationsContract.Table.COL_LATITUDE + " TEXT,"
            + LocationsContract.Table.COL_LONGITUDE + " TEXT,"
            + LocationsContract.Table.COL_ALTITUDE + " TEXT,"
            + LocationsContract.Table.COL_SPEED + " TEXT )";


    private DbBuilder() {
    }


}
