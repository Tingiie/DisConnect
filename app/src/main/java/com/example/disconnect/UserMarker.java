package com.example.disconnect;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class UserMarker implements ClusterItem {
    private LatLng position;
    private String title;
    private int icon;
    private User user;

    public UserMarker(LatLng position, String title, int icon, User user) {
        this.position = position;
        this.title = title;
        this.icon = icon;
        this.user = user;
    }

    public UserMarker() {

    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
