/*

This class uses the package Sensey (https://github.com/nisrulz/sensey) under the following license:

Copyright 2016 Nishant Srivastava

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
limitations under the License.

*/

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
