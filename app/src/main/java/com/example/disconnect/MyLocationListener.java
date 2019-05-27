package com.example.disconnect;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class MyLocationListener extends AppCompatActivity implements LocationListener {
    private double latitude;
    private double longitude;
    private NavMapActivity mapActivity;
    private LatLng latlng;

    public MyLocationListener(NavMapActivity activity) {
        mapActivity = activity;
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        latlng = new LatLng(latitude, longitude);
        mapActivity.updateDeviceLocation();
        mapActivity.centerMap(latlng);

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
//        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//        startActivity(intent);
    }
}
