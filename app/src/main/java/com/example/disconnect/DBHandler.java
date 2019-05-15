package com.example.disconnect;

import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class DBHandler extends AppCompatActivity {


    //widgets
    private EditText mMessage;

    //vars
    private ListenerRegistration mUserListEventListener;
    private FirebaseFirestore mDb;
    private ArrayList<User> mUserList = new ArrayList<>();
    private ArrayList<UserLocation> mUserLocations = new 

}
