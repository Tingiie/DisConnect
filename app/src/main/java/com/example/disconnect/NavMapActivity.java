package com.example.disconnect;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import java.util.ArrayList;
import java.util.Calendar;

public class NavMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "NavMapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_REQ_CODE = 1234;
    private static final float DEFAULT_ZOOM = 17.5f;
    private static final int RADIUS = 75;
    private static final int maxDistance = 50;
    private static final int distantMarker = R.drawable.gray_marker;
    private static final int nearbyMarker = R.drawable.pngrosa;
    private static final int outlinedMarker = R.drawable.bigdot;
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
    private User potentialMatch;
    private String potentialMatchId;
    private String empty = "empty";
    private final DBHandler dbHandler = new DBHandler();
    private boolean hasPotentialMatch;
    private Marker currentMarker;
    private String currentMarkerTag;
    private UpdateInformationTimer timer;
    private boolean hasVibrated = false;
    private int timerCounter;
    private boolean markerLock = false;
    private Dialog myDialog;
    private int connectionCounter = 0;
    private boolean connected;

    // User object representing user of current session
    private User mUser;

    //Contains all users currently in Firebase.
    private ArrayList<User> allUsersList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_map);

        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        HandshakeDetector handshakeDetector = new HandshakeDetector(this);

        dbHandler.setmDb(mDb);
        dbHandler.setActivity(this);
        dbHandler.getUser();

        myDialog = new Dialog(this);

        UpdateInformationTimer timer = new UpdateInformationTimer(5000, 200);
        timer.start();

        statusOnline();

        timerCounter = 0;
        locationListener = new MyLocationListener(this);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        nearbyUsers = new ArrayList<>();
        potentialMatchId = empty;
        hasPotentialMatch = false;

        FloatingActionButton gpsButton = findViewById(R.id.gps_button);
        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dbHandler.updateUser(mUser);

                if (!mLocationPermissionGranted) {
                    getLocationPermission();
                    initMap();
                }

                shareLocation = !shareLocation;

                if (hasPermissionAndLocation()) {
                    if (shareLocation) {
                        Log.d(TAG, "onClick: sharelocation = true, status is set to online");
                        statusOnline();
                        Toast.makeText(NavMapActivity.this, "Your location is visible to other users", Toast.LENGTH_SHORT).show();
                        resetMap();
                        enableMapLocation(true);
                        shareLocation = true;
                    } else {
                        Toast.makeText(NavMapActivity.this, "Your location is hidden from other users", Toast.LENGTH_SHORT).show();
                        statusOffline();
                        mMap.clear();
                        enableMapLocation(false);
                        shareLocation = false;
                        potentialMatchId = empty;
                        mUser.setPotentialMatch(empty);
                        hasPotentialMatch = false;
                        dbHandler.updateUser(mUser);
                        //mUser.setActive(false);
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

    //Menyraden
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out: {
                signOut();
                return true;
            }
            case R.id.helpful_action:{
                startActivity(new Intent(this, activity_info1.class));
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
        Log.d(TAG, "signOut: userId: " + mUser.getUser_id());
        if (mUser != null) {
            mUser.setActive(false);
            dbHandler.updateUser(mUser);
        }
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
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d(TAG, "onMarkerClick: update potential match");
                updatePotentialMatch(marker);
                return true;
            }
        });

        if (hasPermissionAndLocation()) {
            Log.d(TAG, "onMapReady: hasPermissionAndLocation = true");
            Log.d(TAG, "onMapReady: status is set to online");
            statusOnline();
            setMapSettings();
            updateDeviceLocation();
            enableMapLocation(true);
            shareLocation = true;
            setCircle();
            centerMap(currentLatLng);
        } else {
            Log.d(TAG, "onMapReady: hasPermissionAndLocation = false");
            statusOffline();
        }
    }

    public boolean updateDeviceLocation() {
        Log.d(TAG, "updateDeviceLocation: getting the device's current location");

        try {
            if (hasPermissionAndLocation()) {
                locationManager.requestLocationUpdates("gps",
                        2000,
                        0, locationListener);
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                if (mUser != null) {
                    mUser.setLocation(currentLatLng);
                    Log.d(TAG, "updateDevicelocation: mUser != null");
                    centerMap(currentLatLng);
                    Log.d(TAG, "updateDevicelocation: resetStatus");
                    resetStatus();
                    updateNearbyUsers();
                }
                return true;
            } else {
                statusOffline();
                Log.d(TAG, "updateDeviceLocation: current location is null");
                //Toast.makeText(NavMapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (SecurityException e) {
            statusOffline();
            Log.d(TAG, "updateDeviceLocation: SecurityException: " + e.getMessage());
            //Toast.makeText(NavMapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
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
                .radius(RADIUS)
                .strokeColor(Color.argb(150, 73, 95, 106))
                .fillColor(Color.argb(50, 73, 95, 106)));
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
        Log.d(TAG, "statusOffline: entered");
        status = "Offline";
        setTitle(status);
        if (mUser != null) {
            mUser.setActive(false);
            potentialMatchId = empty;
            mUser.setPotentialMatch(empty);
            hasPotentialMatch = false;
            dbHandler.updateUser(mUser);
        }
    }

    private void statusOnline() {
        Log.d(TAG, "statusOnline: entered");
        status = "Online";
        setTitle(status);
        if (mUser != null) {
            Log.d(TAG, "statusOnline: user is not null. active is set to true");
            mUser.setActive(true);
            dbHandler.updateUser(mUser);
        }
    }

    private void statusNearbyUsers(int count) {
        Log.d(TAG, "statusNearbyUsers: entered");
        Log.d(TAG, "statusNearbyUsers hasPotentialMatch = " + hasPotentialMatch);
        if (hasPotentialMatch) {
            statusAwaitingHandshake();
            return;
        }
        status = "Nearby users: " + count;
        setTitle(status);
        if (mUser != null) {
            mUser.setActive(true);
            dbHandler.updateUser(mUser);
        }
    }

    private void statusAwaitingHandshake() {
        Log.d(TAG, "statusAwaitingHandshake: entered");
        status = "Awaiting handshake";
        setTitle(status);
    }

    private void setPotentialMatch(String userId) {
        Log.d(TAG, "setpotentialMatch: entered userId = " + userId);

        if (userId.equals("empty")) {
            Log.d(TAG, "setpotentialMatch: entered empty");
            potentialMatchId = empty;
            mUser.setPotentialMatch(empty);
            hasPotentialMatch = false;
            dbHandler.updateUser(mUser);
        } else {
            Log.d(TAG, "setpotentialMatch: userId " + userId);
            potentialMatchId = userId;
            mUser.setPotentialMatch(userId);
            hasPotentialMatch = true;
            dbHandler.updateUser(mUser);
        }
    }

    private void updatePotentialMatch(Marker marker) {
        Log.d(TAG, "updatePotentialMatch: entered");
        String markerId = (String) marker.getTag();

        Log.d(TAG, "updatePotentialMatch: marker tag = " + markerId);

        if (nearbyUsers.isEmpty()) {
            Log.d(TAG, "updatePotentialMatch: no nearby users");
            return;
        }
        markerLock = true;
        if (potentialMatchId.equals(empty)) {
            Log.d(TAG, "updatePotentialMarker: Scenario 1");
            Log.d(TAG, "updatePotentialMatch: potentialMatch is empty");
            currentMarker = marker;
            currentMarkerTag = markerId;
            currentMarker.setIcon(BitmapDescriptorFactory.fromResource(outlinedMarker));
            setPotentialMatch(currentMarkerTag);
            hasPotentialMatch = true;
            statusAwaitingHandshake();
            Log.d(TAG, "updatePotentialMarker: End of Scenario 1");
        } else if (potentialMatchId.equals(markerId)) {
            Log.d(TAG, "updatePotentialMarker: Scenario 2");
            Log.d(TAG, "updatePotentialMatch: potentialMatch is currentMarker");
            currentMarker.remove();
            User clickedUser = null;
            for (User user : allUsersList) {
                if (user.getUser_id().equals(markerId)) {
                    clickedUser = user;
                }
            }

            if (clickedUser != null) {
                Log.d(TAG, "updatePotentialMarker: clickedUser is not null");
                MarkerOptions opt = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(nearbyMarker))
                        .position(new LatLng(clickedUser.getLocation().latitude, clickedUser.getLocation().longitude));
                Marker newMarker = mMap.addMarker(opt);
                newMarker.setTag(clickedUser.getUser_id());
                currentMarker = null;
                currentMarkerTag = empty;
                potentialMatchId = empty;
                mUser.setPotentialMatch(empty);
                hasPotentialMatch = false;
            }
            Log.d(TAG, "updatePotentialMarker: clickedUser is null");
            Log.d(TAG, "updatePotentialMarker: End of Scenario 2");
        } else {
            Log.d(TAG, "updatePotentialMarker: Scenario 3");
            Log.d(TAG, "updatePotentialMatch: potentialMatch is not null nor currentMarker");

            User oldUser = null;
            for (User user : allUsersList) {
                if (user.getUser_id().equals(currentMarkerTag)) {
                    oldUser = user;
                }
            }

            if (oldUser != null && currentMarker != null) {
                currentMarker.remove();
                MarkerOptions opt = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(nearbyMarker))
                        .position(new LatLng(oldUser.getLocation().latitude, oldUser.getLocation().longitude));
                Marker oldMarker = mMap.addMarker(opt);
                oldMarker.setTag(oldUser.getUser_id());
                currentMarkerTag = empty;

                currentMarker = marker;
                currentMarkerTag = markerId;
                currentMarker.setIcon(BitmapDescriptorFactory.fromResource(outlinedMarker));
                setPotentialMatch(currentMarkerTag);
                potentialMatchId = currentMarkerTag;
                hasPotentialMatch = true;
                statusAwaitingHandshake();
            }
            Log.d(TAG, "updatePotentialMarker: End of Scenario 3");
        }
        dbHandler.updateUser(mUser);
        resetMap();
        markerLock = false;
    }

    private void setPotentialMarker(Marker marker) {
        String userId = (String) marker.getTag();
        setPotentialMatch(userId);
        currentMarker = marker;
        marker.setIcon(BitmapDescriptorFactory.fromResource(outlinedMarker));
        hasPotentialMatch = true;
    }

    private void resetPotentialMarker() {
        if (currentMarker != null) {
            String userId = (String) currentMarker.getTag();
            currentMarker.setIcon(BitmapDescriptorFactory.fromResource(nearbyMarker));
            currentMarker = null;
            potentialMatchId = empty;
            setPotentialMatch(empty);
            hasPotentialMatch = false;
        }
    }

    public void updateNearbyUsers() {
        if (allUsersList == null || allUsersList.isEmpty()) {
            return;
        }

        boolean oldListEmpty;
        if (nearbyUsers == null) {
            oldListEmpty = true;
        } else {
            if (nearbyUsers.isEmpty()) {
                oldListEmpty = true;
            } else {
                oldListEmpty = false;
            }
        }

        nearbyUsers = new ArrayList<>();
        for (User user : allUsersList) {
            LatLng otherLocation = user.getLocation();
            double distance = locationDistance(currentLatLng.latitude, currentLatLng.longitude, otherLocation.latitude, otherLocation.longitude);

            if (!user.getUser_id().equals(mUser.getUser_id())) {
                if (user.isActive() && distance < RADIUS) {
                    if (distance <= maxDistance) {
                        if (hasPotentialMatch && user.getUser_id().equals(potentialMatchId)) {
                            nearbyUsers.add(user);
                            createPotentialMarker(user);
                        } else {
                            nearbyUsers.add(user);
                            createNearbyMarker(user);
                        }
                    } else {
                        createDistantMarker(user);
                    }
                }
            }
        }

        if (!nearbyUsers.isEmpty()) {
            statusNearbyUsers(nearbyUsers.size());
        }

        if (!nearbyUsers.isEmpty()) {
            if ((oldListEmpty || !nearbyUsers.isEmpty()) && !hasVibrated) {    //if oldlist.isEmpty || !nearbyUsers.isEmpty
                int nUsers = nearbyUsers.size();
                if (nUsers == 1) {
                    Toast.makeText(NavMapActivity.this, "A user is nearby!", Toast.LENGTH_LONG).show();
                } else if (nUsers > 1) {
                    Toast.makeText(NavMapActivity.this, nUsers + " users are nearby!", Toast.LENGTH_LONG).show();
                }
                vibrate(500);
                hasVibrated = true;
            }
        } else {
            hasVibrated = false;
        }
    }


    public void showPopup() {
        TextView txtclose;
        Button btn;
        myDialog.setContentView(R.layout.activity_custom_pop);
        txtclose = (TextView) myDialog.findViewById(R.id.txtclose);
        txtclose.setText("X");
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
                mUser.setHandshakeDetected(false);
                hasPotentialMatch = false;
                potentialMatchId = empty;
                mUser.setPotentialMatch(empty);
                dbHandler.updateUser(mUser);
                resetMap();
                resetStatus();
            }
        });
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    public void createNearbyMarker(User user) {
        MarkerOptions opt = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(nearbyMarker))
                .position(new LatLng(user.getLocation().latitude, user.getLocation().longitude));
        Marker marker = mMap.addMarker(opt);
        marker.setTag(user.getUser_id());
    }

    public void createDistantMarker(User user) {
        MarkerOptions opt = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(distantMarker))
                .position(new LatLng(user.getLocation().latitude, user.getLocation().longitude));

        Marker marker = mMap.addMarker(opt);
        marker.setTag(user.getUser_id());
    }

    public void createPotentialMarker(User user) {
        MarkerOptions opt = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(outlinedMarker))
                .position(new LatLng(user.getLocation().latitude, user.getLocation().longitude));

        Marker marker = mMap.addMarker(opt);

        marker.setTag(user.getUser_id());
        potentialMatchId = user.getUser_id();
        mUser.setPotentialMatch(potentialMatchId);
        hasPotentialMatch = true;
    }

    private void vibrate(long time) {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(time);
            }
        } catch (Exception e) {
            Log.d(TAG, "vibrate: error message " + e.getMessage());
        }
    }

    private double locationDistance(double lat1, double lon1, double lat2, double lon2) {
        double dist = 0;
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return dist;
        } else {
            double theta = lon1 - lon2;
            dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1))
                    * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515 * 1.609344 * 1000; //m
            return (dist);
        }
    }

    public void setCurrentUser(User user) {
        this.mUser = user;
    }

    public void setAllUsersList(ArrayList<User> allUsersList) {
        this.allUsersList = allUsersList;
    }

    public void onHandshake() {
        Log.d(TAG, "onHandshake: hasPotentialMatch = " + hasPotentialMatch);
        if (!hasPotentialMatch || !mUser.isActive()) {
            return;
        }
        try {
            for (User user : allUsersList) {
                if (user.getUser_id().equals(potentialMatchId)) {
                    potentialMatch = user;
                }
            }
            String potentialMeId = potentialMatch.getPotentialMatch();

            if (mUser.getUser_id().equals(potentialMeId)) {
                mUser.setHandshakeDetected(true);
                mUser.setHandShakeTime(Calendar.getInstance().getTime());
                dbHandler.updateUser(mUser);

                HandshakeTimer h = new HandshakeTimer(5000, 1000);
                h.start();
            }
        } catch (Exception e) {
            Log.d(TAG, "onHandshake: potentialMatch's something is null");
            return;
        }
    }

    private void resetStatus() {
        Log.d(TAG, "resetStatus: entered");
        if (nearbyUsers.isEmpty()) {
            statusOnline();
        } else if (hasPotentialMatch) {
            statusAwaitingHandshake();
        } else {
            statusNearbyUsers(nearbyUsers.size());
        }
    }

    private class HandshakeTimer extends CountDownTimer {
        public HandshakeTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (!hasPotentialMatch) {
                resetStatus();
                return;
            }

            dbHandler.getAllUsers();

            for (User u : allUsersList) {
                if (u.getUser_id().equals(potentialMatchId)) {
                    potentialMatch = u;
                }
            }

            String potentialMeId = potentialMatch.getPotentialMatch();
            //long handshakeTimeDiff = Math.abs(potentialMatch.getHandShakeTime().getTime() - mUser.getHandShakeTime().getTime());

            //if (mUser.getUser_id().equals(potentialMeId) && potentialMatch.isActive() && potentialMatch.isHandshakeDetected() && handshakeTimeDiff < 10000) {
            if (mUser.getUser_id().equals(potentialMeId) && potentialMatch.isActive() && potentialMatch.isHandshakeDetected()) {

                mUser.incConnectionCounter();
                connectionCounter++;
                if (connectionCounter == 1) {
                    connected = true;
                    vibrate(500);
                    showPopup();
                }
            }
        }

        @Override
        public void onFinish() {
            mUser.setHandshakeDetected(false);
            hasPotentialMatch = false;
            potentialMatchId = empty;
            mUser.setPotentialMatch(empty);
            dbHandler.updateUser(mUser);
            mMap.clear();
            setMapSettings();
            setCircle();
            centerMap(currentLatLng);
            resetStatus();
            if (connected) {
                connectionCounter = 0;
            }
        }
    }

    

    private class UpdateInformationTimer extends CountDownTimer {

        public UpdateInformationTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            try {
                if (mUser != null) {
                    timerCounter++;
                }

                if (markerLock) {
                    start();
                    return;
                }

                if (mUser != null && mUser.isActive()) {
                    resetMap();
                    updateDeviceLocation();
                    dbHandler.updateUser(mUser);
                    dbHandler.getAllUsers();
                } else if (mUser != null) {
                    dbHandler.updateUser(mUser);
                    mMap.clear();
                    setMapSettings();
                }

                if (timerCounter == 1) {
                    potentialMatchId = empty;
                    mUser.setPotentialMatch(empty);
                    hasPotentialMatch = false;
                    mUser.setHandshakeDetected(false);
                    dbHandler.updateUser(mUser);
                    initMap();
                }

                if (hasPotentialMatch) {
                    statusAwaitingHandshake();
                }
                start();
            } catch (Exception e) {
                Log.e("Error", "Error: " + e.toString());
            }
        }
    }
}
