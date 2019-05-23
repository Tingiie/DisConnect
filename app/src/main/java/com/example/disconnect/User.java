package com.example.disconnect;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class User implements Parcelable{

    private String email;
    private String user_id;
    private String username;
    private boolean active;
    private Location location;
    private boolean handshakeDetected;
    private Date handShakeTime;
    private User potentialMatch;
    private int connectionCounter;
    private GeoPoint geo_point = new GeoPoint(1.0,2.0);
    private @ServerTimestamp
    Date timestamp;



    //  private String avatar;

    public User( boolean active, int connectionCounter , String email,Date handShakeTime, boolean handshakeDetected,User potentialMatch,  String user_id, String username, GeoPoint geo_point, Date timestamp ) {
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

    public User() {

    }

    protected User(Parcel in) {
        email = in.readString();
        user_id = in.readString();
        username = in.readString();
        active = Boolean.parseBoolean(in.readString());
        timestamp = null;

        // avatar = in.readString();
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
    /*
        public String getAvatar() {
            return avatar;
        }
        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
    */
    public static Creator<User> getCREATOR() {
        return CREATOR;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setActive(boolean active){
        this.active = active;
    }

    public boolean getActive(){
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
        //", avatar='" + avatar + '\'' +
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
        // dest.writeString(avatar);
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Date getHandShakeTime() {
        return handShakeTime;
    }

    public void setHandShakeTime(Date handShakeTime) {
        this.handShakeTime = handShakeTime;
    }

    public User getPotentialMatch() {
        return potentialMatch;
    }

    public void setPotentialMatch(User potentialMatch) {
        this.potentialMatch = potentialMatch;
    }

    public int getConnectionCounter() {
        return connectionCounter;
    }

    public void incConnectionCounter() {
        this.connectionCounter++;
    }

    
}