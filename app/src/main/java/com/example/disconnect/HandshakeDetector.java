package com.example.disconnect;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.disconnect.NavMapActivity;
import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.ShakeDetector;
import com.google.android.gms.maps.model.LatLng;


public class HandshakeDetector implements ShakeDetector.ShakeListener {

    private NavMapActivity activity;
    private LatLng islamabad = new LatLng(33, 73);

    public HandshakeDetector(NavMapActivity activity){
        this.activity = activity;
        Sensey.getInstance().init(activity);
        Sensey.getInstance().startShakeDetection(10f, 2000,this);

    }

    @Override

    public void onShakeDetected() {
        //Should probably trigger on onShakeStopped instead, test and reevaluate.
        activity.onHandshake();

    }

    @Override
    public void onShakeStopped() {

    }
}
