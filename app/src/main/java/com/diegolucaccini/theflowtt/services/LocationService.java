package com.diegolucaccini.theflowtt.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.diegolucaccini.theflowtt.utils.GoogleApiClientHelper;
import com.diegolucaccini.theflowtt.utils.JourneyTracker;
import com.google.android.gms.location.LocationListener;

import java.util.Random;

/**
 * Created by Diego Lucaccini on 30/06/2017.
 */

public class LocationService extends Service implements LocationListener {

    public static final String BROADCASTED_LOCATION = "loc_brd";
    private static final String TAG = LocationService.class.getSimpleName();
    private static final float MIN_ACCURACY_TRESHOLD = 20f;
    private static final float MIN_DISTANCE_TO_TRESHOLD = 2f;

    private static final boolean IS_DEBUG_MODE = false;

    private static JourneyTracker journeyTracker;

    private static Location mLastLocation;
    double lastLat = 41.821388 + ((float) (Math.random() * 0.2));
    double lastLon = 12.520054 + ((float) (Math.random() * 0.2));

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "****** LocationService onCreate");
        GoogleApiClientHelper.getInstance(this).startTrackingJourney(this);

        journeyTracker = JourneyTracker.getInstance();
        journeyTracker.initJourney(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "****** LocationService onDestroy");
        GoogleApiClientHelper.getInstance(this).stopTrackingJourney(this);

        journeyTracker.finalizeJourney();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {


        return null;
    }


    @Override
    public void onLocationChanged(Location location) {

        if (IS_DEBUG_MODE) {
            Random r = new Random();
            double rangeMin = 0.001;
            double rangeMax = 0.004;
            location.setSpeed(1.0f);
            lastLat += (rangeMin + (rangeMax - rangeMin) * r.nextDouble());
            lastLon += (rangeMin + (rangeMax - rangeMin) * r.nextDouble());
            location.setLatitude(lastLat);
            location.setLongitude(lastLon);
            location.setAccuracy(8.0f);
        }

        Log.d(TAG, "onLocationChanged [" + location.getAccuracy() + "] distance to last [" + (mLastLocation != null ? location.distanceTo(mLastLocation) : -1) + "]");

        Intent broadcastIntent = new Intent(BROADCASTED_LOCATION);
        broadcastIntent.putExtra("location", location);
        broadcastIntent.putExtra("distance", (mLastLocation != null ? location.distanceTo(mLastLocation) : -1f));
        sendBroadcast(broadcastIntent);

        if (isLocationGood(location)) {

            journeyTracker.addPosition(location);
            mLastLocation = location;

        }

    }

    private boolean isLocationGood(Location l) {

        return l.getAccuracy() < MIN_ACCURACY_TRESHOLD && (mLastLocation == null || l.distanceTo(mLastLocation) > MIN_DISTANCE_TO_TRESHOLD);

    }

}
