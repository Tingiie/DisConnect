package com.example.disconnect;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

public class NavMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "NavMapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_REQ_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

    private GoogleMap mMap;
    private LocationManager locationManager;
    private MyLocationListener locationListener;
    private boolean sharedLocation = false;
    private LatLng currentLatLng;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_map);

        locationListener = new MyLocationListener();
        locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        FloatingActionButton gpsButton = findViewById(R.id.gps_button);

        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedLocation = !sharedLocation;

                if (hasPermissionAndLocation()) {
                    if (sharedLocation) {
                        enableMapLocation(true);
                        initMap();
                        sharedLocation = true;
                        Toast.makeText(NavMapActivity.this, "Your location is visible to other users", Toast.LENGTH_SHORT).show();
                    } else {
                        mMap.clear();
                        enableMapLocation(false);
                        sharedLocation = false;
                        Toast.makeText(NavMapActivity.this, "Your location is hidden from other users", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(NavMapActivity.this, "Please turn on Location", Toast.LENGTH_LONG).show();
                    mMap.clear();
                    enableMapLocation(false);
                }
            }
        });

        initMap();
    }

    private void initMap() {
        Log.d(TAG, "initMap: map is initialized");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(NavMapActivity.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (hasPermissionAndLocation()) {
            setMapSettings();
            updateDeviceLocation();
            enableMapLocation(true);
            sharedLocation = true;
            setCircle();
        } else {
            Toast.makeText(this, "Please turn on Location", Toast.LENGTH_LONG).show();
        }
    }

    private void updateDeviceLocation() {
        Log.d(TAG, "updateDeviceLocation: getting the device's current location");

        try {
            if (hasPermissionAndLocation()) {
                locationManager.requestLocationUpdates("gps",
                        2000,
                        0, locationListener);
                Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                moveCamera(currentLatLng, DEFAULT_ZOOM);
            } else {
                Log.d(TAG, "updateDeviceLocation: current location is null");
                Toast.makeText(NavMapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
        Log.d(TAG, "updateDeviceLocation: SecurityException: " + e.getMessage());
        Toast.makeText(NavMapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();

        }
    }

    private void moveCamera(LatLng latlng, float zoom){
        Log.d(TAG, "moveCamera: moving the camera to Latitude: " + latlng.latitude + ", longitude: " + latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
    }

    private void setMapSettings() {
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
    }

//    private void resetMap() {
//        mMap.clear();
//        setMapSettings();
//        setCircle();
//        updateDeviceLocation();
//    }

    private void setCircle() {
        mMap.addCircle(new CircleOptions()
                .center(currentLatLng)
                .radius(300)
                .strokeColor(Color.argb(150,00,100, 210))
                .fillColor(Color.argb(50,00,100, 210)));
    }

    private boolean hasPermissionAndLocation() {
            return (ActivityCompat.checkSelfPermission(this, FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED && locationManager.isLocationEnabled());
    }

    private boolean enableMapLocation(boolean status) {
        try {
            mMap.setMyLocationEnabled(status);
            return true;
        } catch (SecurityException e) {
            Log.d(TAG, "enableMapLocation: SecurityError: " + e.getMessage());
            return false;
        }
    }
    

//    private void getLocationPermission() {
//        Log.d(TAG, "getLocationPermission: getting location permissions");
//        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
//        Manifest.permission.ACCESS_COARSE_LOCATION};
//
//        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                mLocationPermissionGranted = true;
//                initMap();
//            } else {
//                ActivityCompat.requestPermissions(this, permissions, LOCATION_REQ_CODE);
//            }
//        } else {
//            ActivityCompat.requestPermissions(this, permissions, LOCATION_REQ_CODE);
//        }
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        Log.d(TAG, "onRequestPermissionsResult: called");
//        mLocationPermissionGranted = false;
//
//        switch(requestCode) {
//            case  LOCATION_REQ_CODE :{
//                if (grantResults.length > 0) {
//                    for (int i = 0; i < grantResults.length; i++) {
//                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                            mLocationPermissionGranted = false;
//                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
//                            return;
//                        }
//                    }
//                    mLocationPermissionGranted = true;
//                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
//                    initMap();
//                }
//            }
//        }
//    }
}
