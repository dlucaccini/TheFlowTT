package com.diegolucaccini.theflowtt.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.diegolucaccini.theflowtt.R;
import com.diegolucaccini.theflowtt.dal.DbOpenHelper;
import com.diegolucaccini.theflowtt.services.LocationService;
import com.diegolucaccini.theflowtt.utils.GoogleApiClientHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        ResultCallback<LocationSettingsResult> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;

    private static final float DEFAULT_ZOOM = 18;

    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    private static GoogleApiClientHelper mGoogleApiClientHelper;
    private GoogleMap mGoogleMap;

    private Location mLastKnownLocation;

    private boolean mLocationPermissionGranted;
    private boolean doTrackJourney;

    private Button toggleTrackingBtn;
    private ImageView recordingIcon;
    private FrameLayout dataPanel;
    private TextView accuracyTv;
    private TextView distanceTv;
    private TextView latTv;
    private TextView lonTv;
    private BroadcastReceiver mNewPositionReceiver;
    private LatLng mPrevlatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        doTrackJourney = false;
        toggleTrackingBtn = (Button) findViewById(R.id.toggle_tracking_journey_button);
        recordingIcon = (ImageView) findViewById(R.id.rec_icon);
        dataPanel = (FrameLayout) findViewById(R.id.data_panel);
        accuracyTv = (TextView) findViewById(R.id.acc_tv);
        distanceTv = (TextView) findViewById(R.id.distance_tv);
        latTv = (TextView) findViewById(R.id.lat);
        lonTv = (TextView) findViewById(R.id.lon);

        mGoogleApiClientHelper = GoogleApiClientHelper.getInstance(this);

        DbOpenHelper.getInstance(this);

        toggleTrackingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (doTrackJourney) {
                    stopService(new Intent(MainActivity.this, LocationService.class));
                    doTrackJourney = false;
                    enableTrackLocationGUI(false);
                    if(mPrevlatLng != null) {
                        Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(mPrevlatLng));
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ico_destination));
                    }
                } else {
                    mGoogleMap.clear();
                    mPrevlatLng = null;
                    startService(new Intent(MainActivity.this, LocationService.class));
                    doTrackJourney = true;
                    enableTrackLocationGUI(true);
                }
            }
        });

        initPlayServicesAndMap();

        mNewPositionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                updateGUIwithPosition(intent);

            }
        };
        registerReceiver(mNewPositionReceiver, new IntentFilter(LocationService.BROADCASTED_LOCATION));

    }

    private void updateGUIwithPosition(Intent intent) {


        Location location = intent.getParcelableExtra("location");

        accuracyTv.setText("Accuracy: " + location.getAccuracy());
        String distanceTo = ("" + intent.getFloatExtra("distance", -1f));
        distanceTo = distanceTo.length() > 4 ? distanceTo.substring(0, 4) : distanceTo;
        distanceTv.setText("Distance to last: " + distanceTo);
        latTv.setText("" + location.getLatitude());
        lonTv.setText("" + location.getLongitude());

        if (mGoogleMap != null && doTrackJourney) {

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            if(mPrevlatLng == null) {
                Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(latLng));
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ico_origin));
            }

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

            if(mPrevlatLng != null) {

                PolylineOptions line = new PolylineOptions().add(mPrevlatLng,latLng).width(8).color(Color.BLUE);

                mGoogleMap.addPolyline(line);

            }

            mPrevlatLng = latLng;


        }

    }

    private void initPlayServicesAndMap() {


        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resCode == ConnectionResult.SUCCESS) {

            mGoogleApiClientHelper.connect();

        } else {
            //TODO
            apiAvailability.getErrorDialog(this, resCode, 0);
        }

    }

    private void initLocation() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {

            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            mLastKnownLocation = mGoogleApiClientHelper.getLastKnownLocation(this);

            if (mLastKnownLocation != null) {

                LatLng lastKnownLatLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());

                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, DEFAULT_ZOOM));

            } else {
                Log.d(TAG, "Current location is null. Using defaults.");
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
            }

            mGoogleApiClientHelper.createLocationRequest(this);

        } else {
            mGoogleMap.setMyLocationEnabled(false);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        }

    }


    private void enableTracking(boolean enable) {
        toggleTrackingBtn.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    private void enableTrackLocationGUI(boolean enable) {

        recordingIcon.setVisibility(enable ? View.VISIBLE : View.GONE);
        dataPanel.setVisibility(enable ? View.VISIBLE : View.GONE);
        toggleTrackingBtn.setText(getResources().getString(enable ? R.string.stop_tracking : R.string.start_tracking));

        Toast.makeText(this, enable ? "Tracking Journey" : "Stop tracking", Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_journey_list:
                Intent i = new Intent(MainActivity.this, JourneysListActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /************** PLAY SERVICES CALLBACKS ***************/
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //TODO
        Log.d(TAG, "Play services connection failed: error = " + connectionResult.getErrorCode());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d(TAG, "Connected to Play services");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        //TODO
        Log.d(TAG, "Play services connection suspended");
    }

    /************** GOOGLE MAPS CALLBACKS ***************/

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d(TAG, "Map Ready");

        mGoogleMap = googleMap;

        initLocation();

    }

    /************** PERMISSIONS CALLBACKS ***************/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {

            mLocationPermissionGranted = (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);

            if (mLocationPermissionGranted) {
                initLocation();
            } else {
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
            }

        }

    }

    /************** LocationSettingsRequest CALLBACKS ***************/
    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {

        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                enableTracking(true);
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                enableTracking(false);
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().

                    status.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                enableTracking(false);
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                break;
            default:
                enableTracking(false);
                break;
        }
    }


    /*****************************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        enableTracking(true);
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        Toast.makeText(this, "User agreed to make required location settings changes", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        enableTracking(false);
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        Toast.makeText(this, "User chose not to make required location settings changes.", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNewPositionReceiver);
    }

    public void logDb(View view) {

        DbOpenHelper.getInstance(this).logDatabase();

    }
}
