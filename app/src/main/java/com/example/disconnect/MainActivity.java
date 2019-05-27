package com.example.disconnect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

@IgnoreExtraProperties
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    //  private UserLocation mUserLocation;
    private FirebaseFirestore mDb;
    private FusedLocationProviderClient mFusedLocationClient;
    private User mUser;
    private ArrayList<User> allUsersList;
    private ArrayList<String> idList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDb = FirebaseFirestore.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*

                mUser.setUsername("hasasej");
                updateUser();
                mUser.setEmail("heas@balle.se");
                updateUser();
                mUser.setHandshakeDetected(true);
                updateUser();
                mUser.setActive(false);
                updateUser();

                */


                Log.d(TAG, "Bögballe" + allUsersList.toString());

                Log.d(TAG, "Frodo" + mUser.getEmail());
            }
        });

        getUser();


        getAllUsers();


    }

    /*
        private void getUserDetails(){

            if(mUser == null){
                mUser = new User();
                DocumentReference userRef = mDb.collection(getString(R.string.collection_users))
                        .document(FirebaseAuth.getInstance().getUid());

                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: successfully set the user client.");




                            Boolean active = task.getResult().getBoolean("active");
                            //Boolean active = true;
                            //Sätts till 0 pga null i Firebase. Ska senare använda:
                            int conncount = Math.toIntExact((long) task.getResult().get("connectionCounter"));
                            // int conncount = 0;
                            String email = task.getResult().getString("email");
                            Date handshakeTime = task.getResult().getDate("handShakeTime");


                            Boolean handshakeDetected = task.getResult().getBoolean("handshakeDetected");

                            User potentialMatch = (User) task.getResult().get("potentialMatch");

                            String user_id = task.getResult().getString("user_id");
                            String username = task.getResult().getString("username");
                            GeoPoint geoPoint = task.getResult().getGeoPoint("geo_point");
                            Date timestamp = task.getResult().getDate("timestamp");


                            mUser = new User(active, conncount, email, handshakeTime, handshakeDetected, potentialMatch, user_id, username, geoPoint, timestamp);


                            ((UserClient) (getApplicationContext())).setUser(mUser);

                         //   user = task.getResult().toObject(User.class);
                           // mUser = task.getResult().toObject(User.class);


                            saveUserLocation();
                            //getLastKnownLocation();
                        }

                    }
                });
            }
            else{
                saveUserLocation();
                //  getLastKnownLocation();
            }
        }

    */
    private void getAllUsers() {
        allUsersList = new ArrayList<>();
        CollectionReference usersRef = mDb
                .collection(getString(R.string.collection_users));

        usersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    idList = new ArrayList<>();
                    ArrayList<DocumentSnapshot> resultList = (ArrayList) task.getResult().getDocuments();

                    for (int i = 0; i < resultList.size(); i++) {
                        idList.add(resultList.get(i).getId());
                    }
                    Log.d(TAG, "Idlista" + idList.toString());
                    for (String id : idList) {
                        DocumentReference userRef = mDb.collection(getString(R.string.collection_users))
                                .document(id);

                        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: successfully set the user client.");

                                    Boolean active = task.getResult().getBoolean("active");
                                    //Boolean active = true;
                                    //Sätts till 0 pga null i Firebase. Ska senare använda:
                                    int conncount = Math.toIntExact((long) task.getResult().get("connectionCounter"));
                                    // int conncount = 0;
                                    String email = task.getResult().getString("email");
                                    Date handshakeTime = task.getResult().getDate("handShakeTime");


                                    Boolean handshakeDetected = task.getResult().getBoolean("handshakeDetected");

                                    User potentialMatch = (User) task.getResult().get("potentialMatch");

                                    String user_id = task.getResult().getString("user_id");
                                    String username = task.getResult().getString("username");
                                    GeoPoint geoPoint = task.getResult().getGeoPoint("geo_point");
                                    Date timestamp = task.getResult().getDate("timestamp");


                                    User user = new User(active, conncount, email, handshakeTime, handshakeDetected, potentialMatch, user_id, username, geoPoint, timestamp);
                                    allUsersList.add(user);
                                    Log.d(TAG, "Rövballe" + allUsersList.toString());
                                    Log.d(TAG, "Hästkuk: " + mUser.getUsername());
                                    //    mUser = task.getResult().toObject(User.class);
                                    //                                  Log.d(TAG, "HEJHEJ" + task.getResult().toString());
//                                    Log.d(TAG, "HEJHEJ " + " GEOPOINT " + /*+ task.getResult().getData().containsValue("edvinheterjag@edvin.se")  + */" LATIDUDE: " + task.getResult().getGeoPoint("geo_point").getLatitude() + " DATE: " + task.getResult().getDate("timestamp"));


                                }
                            }
                        });

                    }
                    //Log.d(TAG, "BALLEDRÄNG" + idList.toString());
                }
            }

        });
    }


    private void updateUser() {
        DocumentReference locationRef = mDb
                .collection(getString(R.string.collection_users))
                .document(FirebaseAuth.getInstance().getUid());

        locationRef.set(mUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "saveUserLocation: \ninserted user location into database." +
                            "\n latitude: " + mUser.getGeo_point().getLatitude() +
                            "\n longitude: " + mUser.getGeo_point().getLongitude());
                }
            }
        });


    }

    public void getUser() {
        if (mUser == null) {
            mUser = new User();

            DocumentReference userRef = mDb.collection(getString(R.string.collection_users))
                    .document(FirebaseAuth.getInstance().getUid());

            Log.d(TAG, "1. USERUSER: ");
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {
                        Log.d(TAG, "2. gandalf" + task.getResult().toString());
                        Boolean active = task.getResult().getBoolean("active");
                        Log.d(TAG, "3. getUser: active: " + active);
                        //Boolean active = true;
                        //Sätts till 0 pga null i Firebase. Ska senare använda:
                        int conncount = Math.toIntExact((long) task.getResult().get("connectionCounter"));
                        // int conncount = 0;
                        Log.d(TAG, "4. getUser: count: " + conncount);
                        String email = task.getResult().getString("email");
                        Log.d(TAG, "5. getUser: email: " + email);

                        Date handshakeTime = task.getResult().getDate("handShakeTime");
                        //Log.d(TAG, "getUser: date: " + handshakeTime.toString());  is null
                        Boolean handshakeDetected = task.getResult().getBoolean("handshakeDetected");
                        Log.d(TAG, "6. getUser: handshakeDetected: " + handshakeDetected);
                        User potentialMatch = (User) task.getResult().get("potentialMatch");
                        //Log.d(TAG, "getUser: potentialMatch: " + potentialMatch.getUsername()); is null
                        String user_id = task.getResult().getString("user_id");
                        Log.d(TAG, "7. getUser: user_id: " + user_id);
                        String username = task.getResult().getString("username");
                        Log.d(TAG, "8. getUser: username: " + username);
                        GeoPoint geoPoint = task.getResult().getGeoPoint("geo_point");
                        Log.d(TAG, "9. getUser: geoPoint: " + geoPoint);
                        Date timestamp = task.getResult().getDate("timestamp");
                        Log.d(TAG, "10. getUser: timestamp: " + timestamp);
                        mUser = new User(active, conncount, email, handshakeTime, handshakeDetected, potentialMatch, user_id, username, geoPoint, timestamp);
                        Log.d(TAG, "11. gimli" + mUser.getUser_id() + mUser.getEmail());
                    }
                }
            });
        }
    }

    /*
        private void getLastKnownLocation() {
            Log.d(TAG, "getLastKnownLocation: called.");


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
                @Override
                public void onComplete(@NonNull Task<android.location.Location> task) {
                    if (task.isSuccessful()) {
                        Location location = task.getResult();
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        // Här skickas geopoint och timestamp in i FireBase.
                        if (geoPoint!=null){
                            Log.d(TAG, "its not null");
                            mUser.setGeo_point(geoPoint);
                        }
                        mUser.setTimestamp(null);
                        saveUserLocation();
                    }
                }
            });

        }
    */
/*
    private void saveUserLocation(){
        // Här sparas informationen i FireBase.

        if(mUser != null){
            DocumentReference locationRef = mDb
                    .collection(getString(R.string.collection_users))
                    .document(FirebaseAuth.getInstance().getUid());

            locationRef.set(mUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "saveUserLocation: \ninserted user location into database." +
                                "\n latitude: " + mUser.getGeo_point().getLatitude() +
                                "\n longitude: " + mUser.getGeo_point().getLongitude());
                    }
                }
            });
        }
    }

  */
    public void startMapActivity(View view) {
        //testActivity
        Intent intent = new Intent(this, NavMapActivity.class);
        startActivity(intent);
    }
}
