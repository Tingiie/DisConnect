package com.example.disconnect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
  //  private UserLocation mUserLocation;
    private FirebaseFirestore mDb;
    private FusedLocationProviderClient mFusedLocationClient;
    private User mUser;





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
                startMapActivity(view);
            }
        });

        //getUserDetails();
        User user = getUserInformation();
      //  Log.d(TAG, "User: " + user.getEmail());


    }

    private void getUserDetails(){
            getLastKnownLocation();
    }

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

    public User getUserInformation(){

        DocumentReference drf = mDb
                .collection(getString(R.string.collection_users))
                .document("9XwdGcs6dleIMZmtHy11JBEeQ013"
                );




        Task<DocumentSnapshot> hej = drf.get();
        getLastKnownLocation();
        drf.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                 //   User user = new User();
                 //   user
                 //           .setLocation();


                 //   user = task.getResult().toObject(User.class);

                    Log.d(TAG, "HEJHEJ" + task.getResult().toString());
                    Log.d(TAG, "HEJHEJ " + " GEOPOINT " + /*+ task.getResult().getData().containsValue("edvinheterjag@edvin.se")  + */" " + task.getResult().getGeoPoint("geo_point") + task.getResult().getDate("timestamp"));

                            ;
                }

            }
        });

        return null;
    }






    public void startMapActivity(View view) {
        //testActivity

        Intent intent = new Intent(this, NavMapActivity.class);


        startActivity(intent);
    }
}
