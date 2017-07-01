package com.diegolucaccini.theflowtt.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.diegolucaccini.theflowtt.R;
import com.diegolucaccini.theflowtt.dal.beans.JourneyBean;
import com.diegolucaccini.theflowtt.dal.beans.LocationBean;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class JourneyDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = JourneyDetailActivity.class.getSimpleName();

    private static final float DEFAULT_ZOOM = 15;

    private GoogleMap mGoogleMap;
    private JourneyBean journey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_detail);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        journey = (JourneyBean) getIntent().getSerializableExtra("journey");

        if (journey != null) {

            ((TextView) findViewById(R.id.start)).setText("Started at: " + journey.getStartDate());
            ((TextView) findViewById(R.id.end)).setText("Ended at: " + journey.getEndDate());
            ((TextView) findViewById(R.id.avgAccuracy)).setText("Average accuracy: " + journey.getAvgAccuracy());
            ((TextView) findViewById(R.id.maxSpeed)).setText("Max Speed: " + journey.getMaxSpeed());

        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d(TAG, "Map Ready");

        mGoogleMap = googleMap;
        mGoogleMap.clear();

        ArrayList<LatLng> list = new ArrayList<>();

        for (LocationBean locationBean : journey.getLocationList()) {

            LatLng latLng = new LatLng(locationBean.getLat(), locationBean.getLon());
            Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(latLng));
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.circle_icon));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

            list.add(latLng);

        }

        PolylineOptions line = new PolylineOptions().addAll(list).width(8).color(Color.BLUE);

        mGoogleMap.addPolyline(line);

    }

}
