package com.diegolucaccini.theflowtt.utils;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.diegolucaccini.theflowtt.dal.DbOpenHelper;

import java.util.ArrayList;

/**
 * Created by Diego Lucaccini on 30/06/2017.
 */

public class JourneyTracker {

    private static final String TAG = JourneyTracker.class.getSimpleName();

    private static final int MAX_CHUNK_SIZE = 20;

    private static JourneyTracker instance;
    private static ArrayList<Location> userPositionsChunk;
    private static String currentJourneyId;
    private static DbOpenHelper dbh;

    //using Singleton pattern
    private JourneyTracker() {
        userPositionsChunk = new ArrayList<>();
    }

    public static JourneyTracker getInstance() {

        if (instance == null) {
            instance = new JourneyTracker();
        }

        return instance;

    }

    public void addPosition(Location l) {

        Log.d(TAG, "addPosition [" + l + "]");

        userPositionsChunk.add(l);

        if (userPositionsChunk.size() == MAX_CHUNK_SIZE) {

            saveChunk();

        }

    }

    private void saveChunk() {

        if (!userPositionsChunk.isEmpty()) {

            Log.d(TAG, "* saveChunk");

            dbh.saveLocationsChunk(currentJourneyId, userPositionsChunk);

            userPositionsChunk.clear();

        }

    }

    public void initJourney(Context c) {

        dbh = DbOpenHelper.getInstance(c);
        currentJourneyId = "" + dbh.generateJourneyId();

        Log.d(TAG, "*** initJourney id currentJourneyId " + currentJourneyId);

        dbh.saveJourney(currentJourneyId);

    }


    public void finalizeJourney() {

        Log.d(TAG, "*** finalizeJourney currentJourneyId " + currentJourneyId);
        if (!userPositionsChunk.isEmpty()) {

            saveChunk();

        }

        dbh.finalizeJourney(currentJourneyId);

        userPositionsChunk.clear();


    }

}
