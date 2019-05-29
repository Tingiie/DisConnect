package com.example.disconnect;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@IgnoreExtraProperties
public class User implements Parcelable {

    private String email;
    private String user_id;
    private String username;
    private boolean active;
    private boolean handshakeDetected;
    private Date handShakeTime;
    private String potentialMatch;
    private int connectionCounter;
    private GeoPoint geo_point = new GeoPoint(1.0, 2.0);
    private @ServerTimestamp Date timestamp;

    public User() {

    }

    public User(boolean active, int connectionCounter, String email, Date handShakeTime, boolean handshakeDetected, String potentialMatch, String user_id, String username, GeoPoint geo_point, Date timestamp) {
        this.user_id = user_id;
        this.active = active;
        this.connectionCounter = connectionCounter;
        this.email = email;
        this.username = username;
        this.handshakeDetected = handshakeDetected;
        this.handShakeTime = handShakeTime;
        this.potentialMatch = potentialMatch;
        this.geo_point = geo_point;
        this.timestamp = timestamp;
    }

    protected User(Parcel in) {
        email = in.readString();
        user_id = in.readString();
        username = in.readString();
        active = Boolean.parseBoolean(in.readString());
        timestamp = null;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public static Creator<User> getCREATOR() {
        return CREATOR;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", user_id='" + user_id + '\'' +
                ", username='" + username + '\'' +
                " UserLocation{" +
                ", geo_point=" + geo_point +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(user_id);
        dest.writeString(username);
    }

    public boolean isHandshakeDetected() {
        return handshakeDetected;
    }

    public void setHandshakeDetected(boolean handshakeDetected) {
        this.handshakeDetected = handshakeDetected;
    }

    public GeoPoint getGeo_point() {
        return geo_point;
    }

    public void setGeo_point(GeoPoint geo_point) {
        this.geo_point = geo_point;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setLocation(LatLng latLng) {
        this.geo_point = new GeoPoint(latLng.latitude, latLng.longitude);
    }

    public LatLng getLocation() {
        return new LatLng(this.geo_point.getLatitude(), this.getGeo_point().getLongitude());
    }

    public Date getHandShakeTime() {
        return handShakeTime;
    }

    public void setHandShakeTime(Date handShakeTime) {
        this.handShakeTime = handShakeTime;
    }

    public String getPotentialMatch() {
        return potentialMatch;
    }

    public void setPotentialMatch(String potentialMatch) {
        this.potentialMatch = potentialMatch;
    }

    public int getConnectionCounter() {
        return connectionCounter;
    }

    public void setConnectionCounter(int counter) {
        connectionCounter = counter;
    }

    public void incConnectionCounter() {
        this.connectionCounter++;
    }

    @Override
    public boolean equals(Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of User or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof User)) {
            return false;
        }

        // typecast o to User so that we can compare data members
        User other = (User) o;

        // Compare the data members and return accordingly
        return this.getUser_id().equals(other.equals(getUser_id()));
    }
}

