package com.diegolucaccini.theflowtt.dal.beans;

import java.io.Serializable;

/**
 * Created by Diego Lucaccini on 01/07/2017.
 */

public class LocationBean implements Serializable {

    private double lat;
    private double lon;
    private double accuracy;
    private double speed;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
