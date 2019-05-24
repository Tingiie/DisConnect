package com.example.disconnect;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

public class DBHandler extends AppCompatActivity {


    private static final String TAG = "DBHandler";
    private FirebaseFirestore mDb;
    private User mUser;
    private ArrayList<User> allUsersList;
    private ArrayList<String> idList;
    private String userId;
    private NavMapActivity activity;


    public void setmDb(FirebaseFirestore mDb) {
        this.mDb = mDb;
    }

    public void setUser(String userId){
        this.userId = userId;
    }
    public void setActivity(NavMapActivity activity){
        this.activity = activity;
    }



    public void getUser() {
        if (mUser == null) {
            mUser = new User();

            DocumentReference userRef = mDb.collection(activity.getString(R.string.collection_users))
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
                        activity.setCurrentUser(mUser);
                    }
                }
            });
        }
        //return mUser;
    }

    public void setmUser(User user) {
        mUser = user;
    }

    public void updateUser(User user) {
        mUser = user;
        DocumentReference locationRef = mDb
                .collection(activity.getString(R.string.collection_users))
                .document(FirebaseAuth.getInstance().getUid());

        locationRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    public ArrayList getAllUsers() {
        allUsersList = new ArrayList<>();
        CollectionReference usersRef = mDb
                .collection(activity.getString(R.string.collection_users));

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

        return allUsersList;
    }






}
