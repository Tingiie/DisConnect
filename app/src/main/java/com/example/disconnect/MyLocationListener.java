package com.example.disconnect;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class MyLocationListener implements LocationListener {
    private double latitude;
    private double longitude;
    private NavMapActivity mapActivity;
    private float mapZoom;
    private LatLng latlng;

    public MyLocationListener(NavMapActivity activity, float zoom) {
        mapActivity = activity;
        mapZoom = zoom;
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        latlng = new LatLng(latitude, longitude);
        mapActivity.moveCamera(latlng, mapZoom);
        mapActivity.moveCircle(latlng);
    }

    public double getLongitude(){
        return longitude;
    }


    public double getLatitude(){
        return latitude;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        //Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        //startActivity(intent);
    }

}
