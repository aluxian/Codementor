package com.aluxian.codementor.services;

import android.content.SharedPreferences;

public class UserManager {

    private static final String KEY_FIREBASE_TOKEN = "um_firebase_token";
    private static final String KEY_USERNAME = "um_username";

    private SharedPreferences sharedPrefs;

    private String firebaseToken;
    private String username;

    public UserManager(SharedPreferences sharedPrefs) {
        this.sharedPrefs = sharedPrefs;
        firebaseToken = sharedPrefs.getString(KEY_FIREBASE_TOKEN, null);
        username = sharedPrefs.getString(KEY_USERNAME, null);
    }

    public boolean isLoggedIn() {
        return username != null && firebaseToken != null;
    }

    public String getUsername() {
        return username;
    }

    public void setLoggedIn(String username, String firebaseToken) {
        this.firebaseToken = firebaseToken;
        this.username = username;
        persistState();
    }

    public void setLoggedOut() {
        this.firebaseToken = null;
        this.username = null;
        persistState();
    }

    private void persistState() {
        sharedPrefs.edit()
                .putString(KEY_FIREBASE_TOKEN, firebaseToken)
                .putString(KEY_USERNAME, username)
                .apply();
    }

}
