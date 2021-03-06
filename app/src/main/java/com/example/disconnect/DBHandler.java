package com.example.disconnect;

import android.support.annotation.NonNull;
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
    private NavMapActivity activity;

    public void setmDb(FirebaseFirestore mDb) {
        this.mDb = mDb;
    }

    public void setActivity(NavMapActivity activity) {
        this.activity = activity;
    }

    public void getUser() {
        if (mUser == null) {
            mUser = new User();

            DocumentReference userRef = mDb.collection(activity.getString(R.string.collection_users))
                    .document(FirebaseAuth.getInstance().getUid());

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {
                        Boolean active = task.getResult().getBoolean("active");
                        int conncount = Math.toIntExact((long) task.getResult().get("connectionCounter"));
                        String email = task.getResult().getString("email");
                        Date handshakeTime = task.getResult().getDate("handShakeTime");
                        Boolean handshakeDetected = task.getResult().getBoolean("handshakeDetected");
                        String potentialMatch = task.getResult().getString("potentialMatch");
//                        User potentialMatch = (User) task.getResult().get("potentialMatch");
                        String user_id = task.getResult().getString("user_id");
                        String username = task.getResult().getString("username");
                        GeoPoint geoPoint = task.getResult().getGeoPoint("geo_point");
                        Date timestamp = task.getResult().getDate("timestamp");
                        mUser = new User(active, conncount, email, handshakeTime, handshakeDetected, potentialMatch, user_id, username, geoPoint, timestamp);
                        activity.setCurrentUser(mUser);
                    }
                }
            });
        }
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

    public void getAllUsers() {
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
                    for (String id : idList) {
                        DocumentReference userRef = mDb.collection(activity.getString(R.string.collection_users))
                                .document(id);

                        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    Boolean active = task.getResult().getBoolean("active");
                                    int conncount = Math.toIntExact((long) task.getResult().get("connectionCounter"));
                                    String email = task.getResult().getString("email");
                                    Date handshakeTime = task.getResult().getDate("handShakeTime");
                                    Boolean handshakeDetected = task.getResult().getBoolean("handshakeDetected");
                                    String potentialMatch = task.getResult().getString("potentialMatch");
                                    String user_id = task.getResult().getString("user_id");
                                    String username = task.getResult().getString("username");
                                    GeoPoint geoPoint = task.getResult().getGeoPoint("geo_point");
                                    Date timestamp = task.getResult().getDate("timestamp");
                                    User user = new User(active, conncount, email, handshakeTime, handshakeDetected, potentialMatch, user_id, username, geoPoint, timestamp);
                                    allUsersList.add(user);
                                }
                            }
                        });
                    }
                }
                activity.setAllUsersList(allUsersList);
            }
        });
    }
}
