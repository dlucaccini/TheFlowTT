package com.diegolucaccini.theflowtt.dal.beans;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Diego Lucaccini on 01/07/2017.
 */

public class JourneyBean implements Serializable {

    private String startDate;
    private String endDate;
    private double maxSpeed;
    private double avgAccuracy;
    private ArrayList<LocationBean> locationList;

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public double getAvgAccuracy() {
        return avgAccuracy;
    }

    public void setAvgAccuracy(double avgAccuracy) {
        this.avgAccuracy = avgAccuracy;
    }

    public ArrayList<LocationBean> getLocationList() {
        return locationList;
    }

    public void setLocationList(ArrayList<LocationBean> locationList) {
        this.locationList = locationList;
    }

    @Override
    public String toString() {
        return "JourneyBean{" +
                "startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", maxSpeed=" + maxSpeed +
                ", avgAccuracy=" + avgAccuracy +
                ", locationList size=" + (locationList != null ? locationList.size() : "null") +
                '}';
    }
}
