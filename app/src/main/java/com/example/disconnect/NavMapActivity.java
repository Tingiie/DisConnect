package com.example.disconnect;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class NavMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "NavMapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_REQ_CODE = 1234;
    private static final float DEFAULT_ZOOM = 17f;
    private String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};

    private GoogleMap mMap;
    private LocationManager locationManager;
    private MyLocationListener locationListener;
    private boolean shareLocation = false;
    private LatLng currentLatLng;
    private Location currentLocation;
    private boolean mLocationPermissionGranted = false;
    private Circle mapCircle;
    private ArrayList<User> nearbyUsers;
    private String status;
    private Circle myCircle;
    private User potentialMatch;
    private User nearbyUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_map);

        locationListener = new MyLocationListener(this, DEFAULT_ZOOM);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        nearbyUsers = new ArrayList<>();
        statusOffline();
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
                    statusOnline();
                    if (shareLocation) {
                        Toast.makeText(NavMapActivity.this, "Your location is visible to other users", Toast.LENGTH_SHORT).show();
                        resetMap();
                        enableMapLocation(true);
                        shareLocation = true;
                        try {
                            mockUsers();
                        } catch (Exception e) {
                            Log.d(TAG, "onMapReady: mockUsers, Permission needed?");
                        }
                    } else {
                        Toast.makeText(NavMapActivity.this, "Your location is hidden from other users", Toast.LENGTH_SHORT).show();
                        statusOffline();
                        mMap.clear();
                        enableMapLocation(false);
                        shareLocation = false;
                    }

                } else {
                    statusOffline();
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
            statusOnline();
            setMapSettings();
            updateDeviceLocation();
            enableMapLocation(true);
            shareLocation = true;
            setCircle();
            centerMap(currentLatLng);
        } else {
            statusOffline();
            Toast.makeText(this, "Please turn on Location", Toast.LENGTH_LONG).show();
        }
    }

    public boolean updateDeviceLocation() {
        Log.d(TAG, "updateDeviceLocation: getting the device's current location");

        try {
            if (hasPermissionAndLocation()) {
                statusOnline();
                locationManager.requestLocationUpdates("gps",
                        2000,
                        0, locationListener);
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                return true;
            } else {
                statusOffline();
                Log.d(TAG, "updateDeviceLocation: current location is null");
                Toast.makeText(NavMapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (SecurityException e) {
            statusOffline();
            Log.d(TAG, "updateDeviceLocation: SecurityException: " + e.getMessage());
            Toast.makeText(NavMapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void moveCamera(LatLng latlng, float zoom) {
        //Log.d(TAG, "moveCamera: moving the camera to Latitude: " + latlng.latitude + ", longitude: " + latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
    }

    private void setMapSettings() {
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
//        mMap.getUiSettings().setRotateGesturesEnabled(true);
//        mMap.getUiSettings().setTiltGesturesEnabled(true);
//        mMap.getUiSettings().setCompassEnabled(true);
    }

    private void resetMap() {
        mMap.clear();
        setMapSettings();
        updateDeviceLocation();
        setCircle();
        centerMap(currentLatLng);
    }

    private void setCircle() {
        mapCircle = mMap.addCircle(new CircleOptions()
                .center(currentLatLng)
                .radius(100)
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

    public void centerMap(LatLng center) {
        moveCamera(center, DEFAULT_ZOOM);
        moveCircle(center);
    }

    private void statusOffline() {
        status = "Offline";
        setTitle(status);
    }

    private void statusOnline() {
        status = "Online";
        setTitle(status);
    }

    private void statusNearbyUsers(int count) {
        status = "Nearby users: " + count;
        setTitle(status);
    }

    private void awaitingHandshake() {
        status = "Awaiting handshake";
        setTitle(status);
    }

    private void setPotentialMatch(User user) {
        potentialMatch = user;
    }

    public void updateNearbyUsers(ArrayList<User> allUsers) {
        //Toast.makeText(NavMapActivity.this, "Update nearby users", Toast.LENGTH_SHORT).show();

        if (allUsers.isEmpty()) {
            Log.d(TAG, "updateNearbyUsers: allUsers i null");
            return;
        }

        boolean oldListEmpty;

        if (nearbyUsers == null) {
            oldListEmpty = true;
            Log.d(TAG, "nearbyUsers is null");
        } else {
            if (nearbyUsers.isEmpty()) {
                oldListEmpty = true;
                Log.d(TAG, "Old list is empty");
            } else {
                oldListEmpty = false;
                Log.d(TAG, "Old list is not empty");
            }
        }

        nearbyUsers = new ArrayList<>();
        for (User user : allUsers) {
            LatLng otherLocation = user.getLocation();
            double distance =  locationDistance(currentLatLng.latitude, currentLatLng.longitude, otherLocation.latitude, otherLocation.longitude);
            Log.d(TAG, "Your location is: " + currentLatLng.latitude + " : " + currentLatLng.longitude);
            Log.d(TAG, user.getUsername() + "'s location is: " + otherLocation.latitude + " : " + user.getLocation().longitude);
            Log.d(TAG, "The distance to " + user.getUsername()+ " is: " + Double.toString(distance));

            if (user.getActive() && distance < 100) {
                if (distance <= 20) {
                    nearbyUsers.add(user);
                    createNearbyUser(user);
                } else {
                    createDistantUser(user);
                }
            }
        }

        if (!nearbyUsers.isEmpty()) {
            statusNearbyUsers(nearbyUsers.size());
        }


        if (oldListEmpty && !nearbyUsers.isEmpty()) {
            Toast.makeText(NavMapActivity.this, "A user is nearby!", Toast.LENGTH_LONG).show();
            //Todo: vibrate

            try {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    v.vibrate(500);
                }
            } catch (Exception e) {
                Log.d(TAG, "updateNearbyUsers: error message " + e.getMessage());
            }
        } else if (nearbyUsers.isEmpty()){
            Toast.makeText(NavMapActivity.this, "No user is nearby", Toast.LENGTH_LONG).show();
        }
    }

    public void createNearbyUser(User user) {
        nearbyUser = user;
        CircleOptions nearbyOpt = new CircleOptions()
                .center(new LatLng(user.getLocation().latitude, user.getLocation().longitude))
                .clickable(true)
                .radius(6)
                .strokeColor(Color.WHITE)
                .fillColor(Color.MAGENTA)
                .zIndex(2);
        mMap.addCircle(nearbyOpt);
        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {
                setPotentialMatch(nearbyUser);
                awaitingHandshake();
            }
        });
    }

    public void createDistantUser(User user) {
        CircleOptions distantOpt = new CircleOptions()
                .center(new LatLng(user.getLocation().latitude, user.getLocation().longitude))
                .clickable(false)
                .radius(4)
                .strokeColor(Color.GRAY)
                .fillColor(Color.GRAY)
                .zIndex(2);
        mMap.addCircle(distantOpt);
    }

    private void mockUsers() {
        ArrayList<User> users = new ArrayList<>();

        User user1 = new User("user1@example.com", "1", "user1", true);
        User user2 = new User("user2@example.com", "2", "user2", true);

        LatLng l1 = new LatLng(55.711258, 13.208153);
        user1.setLocation(l1);

        Log.d(TAG, "mockUsers: user1's location is: " + Double.toString(user1.getLocation().latitude) + " : " + Double.toString(user1.getLocation().longitude));

        LatLng l2 = new LatLng(55.711452, 13.209188);
        user2.setLocation(l2);
        Log.d(TAG, "mockUsers: user2's location is: " + Double.toString(user2.getLocation().latitude) + " : " + Double.toString(user2.getLocation().longitude));

        users.add(user1);
        users.add(user2);

        updateNearbyUsers(users);
    }

    //TODO: Check handshake
    // check if potentialMatch isActive
    // check if potentialMatch's potentialMatch is you
    // check if handshakeDetected for you
    // check if handshakeDetected for potentialMatch
    // check if difference between handshakeTime and potentialMatch's handshakeTime <=3 

//    public void createDistantUser() {
//        Location l1 = new Location(currentLocation);
//        LatLng ll1 = new LatLng(55.714911, 13.215717);
//
//        l1.setLatitude(ll1.latitude);
//        l1.setLongitude(ll1.longitude);
//
//        CircleOptions distantOpt = new CircleOptions()
//                .center(ll1)
//                .clickable(false)
//                .radius(10)
//                .strokeColor(Color.LTGRAY)
//                .fillColor(Color.LTGRAY);
//        mMap.addCircle(distantOpt);
//    }
//
//    public void createNearbyUser() {
//        Location l1 = new Location(currentLocation);
//        LatLng ll1 = new LatLng(55.710365, 13.208238);
//
//        l1.setLatitude(ll1.latitude);
//        l1.setLongitude(ll1.longitude);
//
//        CircleOptions nearbyOpt = new CircleOptions()
//                .center(ll1)
//                .clickable(false)
//                .radius(6)
//                .strokeColor(Color.WHITE)
//                .fillColor(Color.MAGENTA)
//                .zIndex(2);
//        myCircle = mMap.addCircle(nearbyOpt);
//        myCircle.setClickable(true);
//        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
//            @Override
//            public void onCircleClick(Circle circle) {
//                //setPotentialMatch();
//                awaitingHandshake();
//            }
//        });
//    }


    private double locationDistance(double lat1, double lon1, double lat2, double lon2) {
        double dist = 0;
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return dist;
        } else {
            double theta = lon1 - lon2;
            dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515 * 1.609344 * 1000; //m
            return (dist);
        }
    }

}
