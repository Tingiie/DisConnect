package com.example.disconnect;

import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.ShakeDetector;
import com.google.android.gms.maps.model.LatLng;

public class HandshakeDetector implements ShakeDetector.ShakeListener {
    private NavMapActivity activity;
    private LatLng islamabad = new LatLng(33, 73);

    public HandshakeDetector(NavMapActivity activity){
        this.activity = activity;
        Sensey.getInstance().init(activity);
        Sensey.getInstance().startShakeDetection(10f, 10,this);
    }

    @Override
    public void onShakeDetected() {
        activity.onHandshake();
    }

    @Override
    public void onShakeStopped() {
    }
}
