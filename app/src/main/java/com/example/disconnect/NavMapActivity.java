package com.example.disconnect;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class NavMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "NavMapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_REQ_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};

    private GoogleMap mMap;
    private LocationManager locationManager;
    private MyLocationListener locationListener;
    private boolean shareLocation = false;
    private LatLng currentLatLng;
    private Location currentLocation;
    private boolean mLocationPermissionGranted = false;
    private Circle mapCircle;
    private ArrayList<User> userList;
    private String status;
    private Circle myCircle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_map);

        locationListener = new MyLocationListener(this, DEFAULT_ZOOM);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        offline();
        FloatingActionButton gpsButton = findViewById(R.id.gps_button);
        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mLocationPermissionGranted) {
                    getLocationPermission();
                    initMap();
                }

                shareLocation = !shareLocation;

                if (hasPermissionAndLocation()) {
                    online();
                    if (shareLocation) {
                        Toast.makeText(NavMapActivity.this, "Your location is visible to other users", Toast.LENGTH_SHORT).show();
                        resetMap();
                        enableMapLocation(true);
                        shareLocation = true;
                    } else {
                        Toast.makeText(NavMapActivity.this, "Your location is hidden from other users", Toast.LENGTH_SHORT).show();

                        mMap.clear();
                        enableMapLocation(false);
                        shareLocation = false;
                    }
                } else {
                    offline();
                    Toast.makeText(NavMapActivity.this, "Please turn on Location", Toast.LENGTH_LONG).show();
                    mMap.clear();
                    enableMapLocation(false);
                }
            }
        });

        getLocationPermission();
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
            online();
            setMapSettings();
            updateDeviceLocation();
            enableMapLocation(true);
            shareLocation = true;
            setCircle();
            mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
                @Override
                public void onCircleClick(Circle circle) {
                    if (circle.equals(myCircle)) {
                        Toast.makeText(NavMapActivity.this, "Status: " + status, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(NavMapActivity.this, "A toast", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            offline();
            Toast.makeText(this, "Please turn on Location", Toast.LENGTH_LONG).show();
        }
    }

    private void updateDeviceLocation() {
        Log.d(TAG, "updateDeviceLocation: getting the device's current location");

        try {
            if (hasPermissionAndLocation()) {
                online();
                locationManager.requestLocationUpdates("gps",
                        2000,
                        0, locationListener);
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                moveCamera(currentLatLng, DEFAULT_ZOOM);
                createNearbyMarker();
                createDistantMarker();
            } else {
                offline();
                Log.d(TAG, "updateDeviceLocation: current location is null");
                Toast.makeText(NavMapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            offline();
            Log.d(TAG, "updateDeviceLocation: SecurityException: " + e.getMessage());
            Toast.makeText(NavMapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
        }
    }

    public void moveCamera(LatLng latlng, float zoom){
        Log.d(TAG, "moveCamera: moving the camera to Latitude: " + latlng.latitude + ", longitude: " + latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
    }

    private void setMapSettings() {
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
    }

    private void resetMap() {
        mMap.clear();
        setMapSettings();
        updateDeviceLocation();
        setCircle();
    }

    private void setCircle() {
        mapCircle = mMap.addCircle(new CircleOptions()
                .center(currentLatLng)
                .radius(300)
                .strokeColor(Color.argb(150,00,100, 210))
                .fillColor(Color.argb(50,00,100, 210)));
    }

    public void moveCircle(LatLng latLng) {
        if (mapCircle != null) {
            mapCircle.setCenter(latLng);
        }
    }


    private boolean hasPermissionAndLocation() {
            return (mLocationPermissionGranted && locationManager.isLocationEnabled());
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

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, permission, LOCATION_REQ_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        mLocationPermissionGranted = false;

        switch(requestCode) {
            case  LOCATION_REQ_CODE :{
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    initMap();
                } else {
                    mLocationPermissionGranted = false;
                    Log.d(TAG, "onRequestPermissionsResult: permission failed");
                    return;
                }
            }
        }
    }

    public void createDistantMarker() {
        Location l1 = new Location(currentLocation);
        LatLng ll1 = new LatLng(55.714911, 13.215717);

        l1.setLatitude(ll1.latitude);
        l1.setLongitude(ll1.longitude);

        CircleOptions distantOpt = new CircleOptions()
                .center(ll1)
                .clickable(false)
                .radius(10)
                .strokeColor(Color.LTGRAY)
                .fillColor(Color.LTGRAY);
        mMap.addCircle(distantOpt);
    }

    public void createNearbyMarker() {
        Location l1 = new Location(currentLocation);
        LatLng ll1 = new LatLng(55.714596, 13.212890);

        l1.setLatitude(ll1.latitude);
        l1.setLongitude(ll1.longitude);


        CircleOptions distantOpt = new CircleOptions()
                .center(ll1)
                .clickable(false)
                .radius(20)
                .strokeColor(Color.WHITE)
                .fillColor(Color.MAGENTA)
                .zIndex(2);
        myCircle = mMap.addCircle(distantOpt);
    }

    private void offline() {
        status = "Offline";
    }

    private void online() {
        status = "Online";
    }

    private void nearbyUsers(int count) {
        status = "Nearby users: " + count;
    }

    private void awaitingHandshake() {
        status = "Awaiting handshake";
    }


//    private void createNearbyMarker(){
//        Location l2 = new Location(currentLocation);
//        LatLng ll2 = new LatLng(55.710365, 13.208238);
//
//        l2.setLatitude(ll2.latitude);
//        l2.setLongitude(ll2.longitude);
//        BitmapDescriptor pinkMarker = BitmapDescriptorFactory.fromResource(R.drawable.pngrosa2);
//
//        MarkerOptions nearbyOpt = new MarkerOptions()
//            .position(ll2)
//            .icon(pinkMarker);
//
//        mMap.addMarker(nearbyOpt);
//    }
}
