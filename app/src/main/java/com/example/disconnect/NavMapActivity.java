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
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

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
    private HashMap<User, Circle> userCircleHashMap;
    private boolean hasPotentialMatch;
    private boolean hasNearbyUsers;

    // User object representing user of current session
    private User mUser;

    //Contains all users currently in Firebase.
    private ArrayList<User> allUsersList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_map);

        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        final DBHandler dbHandler = new DBHandler();
        dbHandler.setmDb(mDb);
        dbHandler.setActivity(this);
        //Log.d(TAG, "Legolas" + "Current user id: " + FirebaseAuth.getInstance().getUid() + "mDb: " + mDb.toString());
        dbHandler.getUser();
        dbHandler.getAllUsers();

        locationListener = new MyLocationListener(this, DEFAULT_ZOOM);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        nearbyUsers = new ArrayList<>();
        statusOffline();

        potentialMatch = new User();
        hasPotentialMatch = false;
        hasNearbyUsers = false;

        FloatingActionButton gpsButton = findViewById(R.id.gps_button);
        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Stuff to test DBHandler
                mUser.setHandshakeDetected(false);
                mUser.setActive(false);
                dbHandler.updateUser(mUser);
                Log.d(TAG, "Elrond " + mUser.getUser_id() + mUser.getEmail() + mUser.getTimestamp());
                Log.d(TAG, "Arwen " + allUsersList.toString());

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
                        mUser.setActive(true);
                        try {
                            mockUsers();
                        } catch (Exception e) {
                            Log.d(TAG, "OnButtonClick Error: " + e.getMessage());
                        }
                    } else {
                        Toast.makeText(NavMapActivity.this, "Your location is hidden from other users", Toast.LENGTH_SHORT).show();
                        statusOffline();
                        mMap.clear();
                        enableMapLocation(false);
                        shareLocation = false;
                        hasPotentialMatch = false;
                        mUser.setActive(false);
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
            statusOnline();
            setMapSettings();
            updateDeviceLocation();
            enableMapLocation(true);
            shareLocation = true;
            setCircle();
            centerMap(currentLatLng);
        } else {
            statusOffline();
           // Toast.makeText(this, "Please turn on Location", Toast.LENGTH_LONG).show();
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
                centerMap(currentLatLng);
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

    private void statusAwaitingHandshake() {
        status = "Awaiting handshake";
        setTitle(status);
    }

    private void updatePotentialMatch(User user) {
        if (hasPotentialMatch && potentialMatch.getUser_id().equals(user.getUser_id())) {
            Log.d(TAG, "updatePotentialMatch: Reset");
            resetPotentialMatch();
            //TODO: if nearby users: status, else online
            if (hasNearbyUsers) {
                statusNearbyUsers(nearbyUsers.size());
            } else {
                statusOnline();
            }
        } else if (hasPotentialMatch) {
            Log.d(TAG, "updatePotentialMatch: Reset and set");
            resetPotentialMatch();
            setPotentialMatch(user);
            statusAwaitingHandshake();
        } else {
            Log.d(TAG, "updatePotentialMatch: Set");
            setPotentialMatch(user);
            statusAwaitingHandshake();
        }
    }

    private void setPotentialMatch(User user) {
        Log.d(TAG, "setPotentialMatch: user's id: " + user.getUser_id());
        potentialMatch = user;
        Circle circle = userCircleHashMap.get(potentialMatch);
        circle.setStrokeColor(Color.WHITE);
        hasPotentialMatch = true;
    }

    private void resetPotentialMatch() {
        Log.d(TAG, "resetPotentialMatch: user's id: " + potentialMatch.getUser_id());
        Circle circle = userCircleHashMap.get(potentialMatch);
        circle.setStrokeColor(Color.MAGENTA);
        hasPotentialMatch = false;
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
            Log.d(TAG, "The distance to " + user.getUsername()+ " is: " + (distance));

            if (user.isActive() && distance < 100) {
                if (distance <= 50) {
                    Log.d(TAG, "user's id: " + user.getUser_id());
                    if (hasPotentialMatch && user.getUser_id().equals(potentialMatch.getUser_id())) {
                        createPotentialUser(user);
                    } else {
                        nearbyUsers.add(user);
                        createNearbyUser(user);
                    }
                } else {
                    createDistantUser(user);
                }
            }
        }

        if (!nearbyUsers.isEmpty()) {
            hasNearbyUsers = true;
            statusNearbyUsers(nearbyUsers.size());
        } else {
            hasNearbyUsers = false;
        }

        //if (oldListEmpty && !nearbyUsers.isEmpty()) {
        if (!nearbyUsers.isEmpty()) {
            int nUsers = nearbyUsers.size();
            if (nUsers == 1) {
                Toast.makeText(NavMapActivity.this, "A user is nearby!", Toast.LENGTH_LONG).show();
            } else if (nUsers > 1) {
                Toast.makeText(NavMapActivity.this, nUsers + " users are nearby!", Toast.LENGTH_LONG).show();
            }

            if (!(!oldListEmpty && nearbyUsers.isEmpty())) {
                vibrate();
            }
        } else if (nearbyUsers.isEmpty()){
            //Toast.makeText(NavMapActivity.this, "No user is nearby", Toast.LENGTH_LONG).show();
        }
    }

    public void createNearbyUser(final User user) {
        nearbyUser = user;
        CircleOptions nearbyOpt = new CircleOptions()
                .center(new LatLng(user.getLocation().latitude, user.getLocation().longitude))
                .clickable(true)
                .radius(6)
                .strokeColor(Color.MAGENTA)
                .fillColor(Color.MAGENTA)
                .zIndex(2);
        Circle circle = mMap.addCircle(nearbyOpt);
        userCircleHashMap.put(user, circle);
        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {
                //setPotentialMatch(nearbyUser);
                updatePotentialMatch(nearbyUser);
            }
        });
    }

    public void createPotentialUser(User user) {
        nearbyUser = user;
        CircleOptions nearbyOpt = new CircleOptions()
                .center(new LatLng(user.getLocation().latitude, user.getLocation().longitude))
                .clickable(true)
                .radius(6)
                .strokeColor(Color.WHITE)
                .fillColor(Color.MAGENTA)
                .zIndex(2);
        Circle circle = mMap.addCircle(nearbyOpt);
        userCircleHashMap.put(user, circle);
        Log.d(TAG, "createNearbyUser: A circle for " + user.getUsername() + " has been created");
        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {
                //setPotentialMatch(nearbyUser);
                updatePotentialMatch(nearbyUser);
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
        Circle circle = mMap.addCircle(distantOpt);
        userCircleHashMap.put(user, circle);
        Log.d(TAG, "createDistantUser: A circle for " + user.getUsername() + " has been created");
    }

    private void mockUsers() {
        ArrayList<User> users = new ArrayList<>();

        //User user1 = new User("user1@example.com", "1", "user1", true);
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setUser_id("1");
        user1.setUsername("user1");
        user1.setActive(true);

        //User user2 = new User("user2@example.com", "2", "user2", true);
        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setUser_id("2");
        user2.setUsername("user2");
        user2.setActive(true);

        //User user3
        User user3 = new User();
        user3.setEmail("user3@example.com");
        user3.setUser_id("3");
        user3.setUsername("user3");
        user3.setActive(true);

        //User user4
        User user4 = new User();
        user4.setEmail("user4@example.com");
        user4.setUser_id("4");
        user4.setUsername("user4");
        user4.setActive(true);

        //User user5
        User user5 = new User();
        user5.setEmail("user5@example.com");
        user5.setUser_id("5");
        user5.setUsername("user5");
        user5.setActive(true);

        LatLng l1 = new LatLng(55.711258, 13.208153);
        user1.setLocation(l1);
        Log.d(TAG, "mockUsers: user1's location is: " + (user1.getLocation().latitude) + " : " + (user1.getLocation().longitude));

        LatLng l2 = new LatLng(55.711452, 13.209188);
        user2.setLocation(l2);
        Log.d(TAG, "mockUsers: user2's location is: " + (user2.getLocation().latitude) + " : " + (user2.getLocation().longitude));

        LatLng l3 = new LatLng(55.711258, 13.209999);
        user3.setLocation(l3);
        Log.d(TAG, "mockUsers: user3's location is: " + (user3.getLocation().latitude) + " : " + (user3.getLocation().longitude));

        LatLng l4 = new LatLng(55.699880, 13.177570);
        user4.setLocation(l4);
        Log.d(TAG, "mockUsers: user4's location is: " + (user4.getLocation().latitude) + " : " + (user4.getLocation().longitude));

        LatLng l5 = new LatLng(55.699916, 13.177638);
        user5.setLocation(l5);
        Log.d(TAG, "mockUsers: user5's location is: " + (user5.getLocation().latitude) + " : " + (user5.getLocation().longitude));

        users.add(user1);
        users.add(user2);
        users.add(user3);
        users.add(user4);
        users.add(user5);
        userCircleHashMap = new HashMap<>();
        updateNearbyUsers(users);
    }

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

    public void setCurrentUser(User user){
        this.mUser = user;
    }

    public void setAllUsersList(ArrayList<User> allUsersList){
        this.allUsersList = allUsersList;
    }

    private void vibrate() {
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
            Log.d(TAG, "vibrate: error message " + e.getMessage());
        }
    }
}
