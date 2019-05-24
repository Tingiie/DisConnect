package com.example.disconnect;

import android.Manifest;
import android.content.Context;
import android.os.CountDownTimer;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

public class NavMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "NavMapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_REQ_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};

    private GoogleMap mMap;
    private LocationManager locationManager;
    private MyLocationListener locationListener;
    private boolean sharedLocation = false;
    private LatLng currentLatLng;
    private boolean mLocationPermissionGranted = false;
    private Circle mapCircle;
    private MyCountDownTimer myCountDownTimer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_map);
        myCountDownTimer = new MyCountDownTimer(10000, 20);
        myCountDownTimer.start();

        locationListener = new MyLocationListener(this, DEFAULT_ZOOM);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        FloatingActionButton gpsButton = findViewById(R.id.gps_button);
        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mLocationPermissionGranted) {
                    getLocationPermission();
                    initMap();
                }

                sharedLocation = !sharedLocation;

                if (hasPermissionAndLocation()) {

                    if (sharedLocation) {
                        Toast.makeText(NavMapActivity.this, "Your location is visible to other users", Toast.LENGTH_SHORT).show();
                        resetMap();
                        enableMapLocation(true);
                        sharedLocation = true;
                    } else {
                        Toast.makeText(NavMapActivity.this, "Your location is hidden from other users", Toast.LENGTH_SHORT).show();

                        mMap.clear();
                        enableMapLocation(false);
                        sharedLocation = false;
                    }
                } else {
                    Toast.makeText(NavMapActivity.this, "Please turn on Location", Toast.LENGTH_LONG).show();
                    mMap.clear();
                    enableMapLocation(false);
                }
            }
        });

        getLocationPermission();
        initMap();
    }


    //Menyraden
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out: {
                signOut();
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    private void signOut() {
        //mUser.setActive(false);
        // updateUser();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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

    public void moveCamera(LatLng latlng, float zoom) {
        Log.d(TAG, "moveCamera: moving the camera to Latitude: " + latlng.latitude + ", longitude: " + latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
    }

    private void setMapSettings() {
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
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
                .strokeColor(Color.argb(150, 00, 100, 210))
                .fillColor(Color.argb(50, 00, 100, 210)));
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

        switch (requestCode) {
            case LOCATION_REQ_CODE: {
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

    public class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            try{
                //FATIMA LÄGG IN METODER HÄÄÄÄR HÄÄÄÄR SKA DE LIGGA
                System.out.println("skrivs ut den var 5e sekund?");
                start();
            }catch(Exception e){
                Log.e("Error", "Error: " + e.toString());
            }

        }
    }
}
