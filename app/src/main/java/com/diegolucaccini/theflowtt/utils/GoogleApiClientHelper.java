package com.diegolucaccini.theflowtt.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.places.Places;

/**
 * Created by Diego Lucaccini on 30/06/2017.
 */

public class GoogleApiClientHelper {

    private static final long UPDATE_INTERVAL = 8000;
    private static final long FASTEST_UPDATE_INTERVAL = 4000;

    private static GoogleApiClientHelper instance;

    private static GoogleApiClient mGoogleApiClient;
    private static LocationRequest mLocationRequest;

    private GoogleApiClientHelper(Context context) {

        //TODO check  GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();

        if (context instanceof GoogleApiClient.ConnectionCallbacks) {

            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) context)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();

        }

    }

    public static GoogleApiClientHelper getInstance(Context context) {

        if (instance == null) {
            instance = new GoogleApiClientHelper(context);
        }

        return instance;

    }

    public void connect() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public void createLocationRequest(ResultCallback<LocationSettingsResult> callbacks) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        locationSettingsRequest
                );

        result.setResultCallback(callbacks);

    }


    public Location getLastKnownLocation(Context context) {

        if (ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        return null;

    }


    public void startTrackingJourney(@NonNull LocationListener locationListener) {

        //TODO  inspection MissingPermission?
        //noinspection MissingPermission
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                locationListener
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                /*doTrackJourney = true;
                enableTrackLocationGUI(true);*/
            }
        });

    }

    public void stopTrackingJourney(@NonNull LocationListener locationListener) {

        //noinspection MissingPermission
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                locationListener
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                /*doTrackJourney = false;
                enableTrackLocationGUI(false);*/
            }
        });
    }


}
