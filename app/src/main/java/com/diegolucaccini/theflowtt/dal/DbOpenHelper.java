package com.diegolucaccini.theflowtt.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.util.Log;

import com.diegolucaccini.theflowtt.dal.beans.JourneyBean;
import com.diegolucaccini.theflowtt.dal.beans.LocationBean;
import com.diegolucaccini.theflowtt.dal.contracts.JourneyContract;
import com.diegolucaccini.theflowtt.dal.contracts.LocationsContract;

import java.util.ArrayList;
import java.util.Date;

import static com.diegolucaccini.theflowtt.dal.DbBuilder.CREATE_JOURNEY_TABLE;
import static com.diegolucaccini.theflowtt.dal.DbBuilder.CREATE_LOCATIONS_TABLE;

/**
 * Created by Diego Lucaccini on 01/07/2017.
 */

public class DbOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = DbOpenHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "the_floowtt_db";
    private static final int DATABASE_VERSION = 1;

    private static DbOpenHelper instance;
    private SQLiteDatabase db;

    private DbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = this.getWritableDatabase();
    }

    public static synchronized DbOpenHelper getInstance(Context context) {

        if (instance == null) {
            instance = new DbOpenHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        Log.d("diego", "SQLiteDatabase onCreate()");

        sqLiteDatabase.execSQL(CREATE_JOURNEY_TABLE);
        sqLiteDatabase.execSQL(CREATE_LOCATIONS_TABLE);

    }

    public void saveLocationsChunk(String journeyId, ArrayList<Location> locationsList) {

        db.beginTransaction();

        for (Location l : locationsList) {

            ContentValues values = new ContentValues();
            values.put(LocationsContract.Table.COL_JOURNEY_ID, journeyId);
            values.put(LocationsContract.Table.COL_ACCURACY, l.getAccuracy());
            values.put(LocationsContract.Table.COL_LATITUDE, l.getLatitude());
            values.put(LocationsContract.Table.COL_LONGITUDE, l.getLongitude());
            values.put(LocationsContract.Table.COL_ALTITUDE, l.getAltitude());
            values.put(LocationsContract.Table.COL_SPEED, l.getSpeed());

            db.insert(LocationsContract.Table.NAME, null, values);

        }

        db.setTransactionSuccessful();
        db.endTransaction();

    }

    public long generateJourneyId() {

        SQLiteStatement s = db.compileStatement("select count(*) from " + JourneyContract.Table.NAME + ";");
        long count = s.simpleQueryForLong();

        return count + 1;

    }

    public void saveJourney(String journeyId) {

        db.beginTransaction();

        ContentValues values = new ContentValues();
        values.put(JourneyContract.Table.COL_JOURNEY_ID, journeyId);
        values.put(JourneyContract.Table.COL_START_JOURNEY, new Date().toString());

        db.insert(JourneyContract.Table.NAME, null, values);

        db.setTransactionSuccessful();
        db.endTransaction();

    }

    public void finalizeJourney(String journeyId) {

        db.beginTransaction();

        ContentValues values = new ContentValues();
        values.put(JourneyContract.Table.COL_END_JOURNEY, new Date().toString());

        String whereClause = JourneyContract.Table.COL_JOURNEY_ID + " = ? ";
        String[] whereArguments = {(journeyId)};

        db.update(JourneyContract.Table.NAME, values, whereClause, whereArguments);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void removeJourney(String journeyId) {

        db.beginTransaction();

        String selection = JourneyContract.Table.COL_JOURNEY_ID + " = ? ";
        String[] selectionArgs = {(journeyId)};

        db.delete(JourneyContract.Table.NAME, selection, selectionArgs);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void removeLocationsByJourneyId(String journeyId) {

        db.beginTransaction();
        String selection = LocationsContract.Table.COL_JOURNEY_ID + " = ? ";
        String[] selectionArgs = {(journeyId)};

        db.delete(LocationsContract.Table.NAME, selection, selectionArgs);
        db.setTransactionSuccessful();
        db.endTransaction();

    }

    public void clearJourneys() {

        db.beginTransaction();

        db.execSQL("delete from " + JourneyContract.Table.NAME);
        db.execSQL("delete from " + LocationsContract.Table.NAME);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public ArrayList<JourneyBean> getJourneys() {

        ArrayList<JourneyBean> journeys = new ArrayList<>();

        Cursor cursor = db.rawQuery("select * from " + JourneyContract.Table.NAME, null);

        if (cursor.moveToFirst()) {
            do {
                String journeyId = cursor.getString(cursor.getColumnIndex(JourneyContract.Table.COL_JOURNEY_ID));
                String start = cursor.getString(cursor.getColumnIndex(JourneyContract.Table.COL_START_JOURNEY));
                String end = cursor.getString(cursor.getColumnIndex(JourneyContract.Table.COL_END_JOURNEY));

                JourneyBean journey = new JourneyBean();
                journey.setStartDate(start);
                journey.setEndDate(end);

                ArrayList<LocationBean> locationsList = new ArrayList<>();

                Cursor locationsCursor = db.rawQuery("select * from " + LocationsContract.Table.NAME + " where " + LocationsContract.Table.COL_JOURNEY_ID + " = ? ", new String[]{journeyId});

                if (locationsCursor.moveToFirst()) {

                    double accuracySum = 0;
                    double maxSpeed = 0;

                    do {
                        double lat = locationsCursor.getDouble(locationsCursor.getColumnIndex(LocationsContract.Table.COL_LATITUDE));
                        double lon = locationsCursor.getDouble(locationsCursor.getColumnIndex(LocationsContract.Table.COL_LONGITUDE));
                        double acc = locationsCursor.getDouble(locationsCursor.getColumnIndex(LocationsContract.Table.COL_ACCURACY));
                        double sp = locationsCursor.getDouble(locationsCursor.getColumnIndex(LocationsContract.Table.COL_SPEED));

                        LocationBean lb = new LocationBean();
                        lb.setLat(lat);
                        lb.setLon(lon);
                        lb.setAccuracy(acc);
                        lb.setSpeed(sp);

                        accuracySum += acc;
                        maxSpeed = sp > maxSpeed ? sp : maxSpeed;

                        locationsList.add(lb);

                    } while (locationsCursor.moveToNext());

                    double avgAccuracy = locationsList.size() > 0 ? accuracySum / locationsList.size() : 0;

                    journey.setAvgAccuracy(avgAccuracy);
                    journey.setMaxSpeed(maxSpeed);

                }

                journey.setLocationList(locationsList);
                journeys.add(journey);

                locationsCursor.close();

            } while (cursor.moveToNext());

        }

        cursor.close();

        return journeys;

    }

    public void logDatabase() {

        Log.d(TAG, "******************** logDatabase ****************************************");
        Log.d(TAG, "------------------------------------------------------------------");
        Log.d(TAG, "<" + JourneyContract.Table.NAME + ">");
        Cursor cursor = db.rawQuery("select * from " + JourneyContract.Table.NAME, null);

        if (cursor.moveToFirst()) {
            do {
                String journeyId = cursor.getString(cursor.getColumnIndex(JourneyContract.Table.COL_JOURNEY_ID));
                String start = cursor.getString(cursor.getColumnIndex(JourneyContract.Table.COL_START_JOURNEY));
                String end = cursor.getString(cursor.getColumnIndex(JourneyContract.Table.COL_END_JOURNEY));
                Log.d(TAG, "journeyId [" + journeyId + "] start [" + start + "] end [" + end + "]");
            } while (cursor.moveToNext());

        }
        cursor.close();

        Log.d(TAG, "------------------------------------------------------------------");
        Log.d(TAG, "<" + LocationsContract.Table.NAME + ">");
        cursor = db.rawQuery("select * from " + LocationsContract.Table.NAME, null);

        if (cursor.moveToFirst()) {
            do {
                String journeyId = cursor.getString(cursor.getColumnIndex(LocationsContract.Table.COL_JOURNEY_ID));
                String lat = cursor.getString(cursor.getColumnIndex(LocationsContract.Table.COL_LATITUDE));
                String lon = cursor.getString(cursor.getColumnIndex(LocationsContract.Table.COL_LONGITUDE));
                String acc = cursor.getString(cursor.getColumnIndex(LocationsContract.Table.COL_ACCURACY));
                String alt = cursor.getString(cursor.getColumnIndex(LocationsContract.Table.COL_ALTITUDE));
                String sp = cursor.getString(cursor.getColumnIndex(LocationsContract.Table.COL_SPEED));
                Log.d(TAG, "journeyId [" + journeyId + "] lat [" + lat + "] lon [" + lon + "] acc [" + acc + "] alt [" + alt + "] sp [" + sp + "]");
            } while (cursor.moveToNext());
        }

        cursor.close();

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
