package com.example.disconnect;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.FirebaseApp;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        FirebaseApp.initializeApp(this);
        findViewById(R.id.textViewSignup).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
     switch (v.getId()){
         case R.id.textViewSignup:

         startActivity(new Intent(this, SignUpActivity.class));
     }
    }
}
