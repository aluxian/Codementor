package com.aluxian.codementor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UserManager {

    private static final String KEY_LOGGED_IN = "um_logged_in";
    private static final String KEY_FIREBASE_TOKEN = "um_firebase_token";
    private static final String KEY_USERNAME = "um_username";

    private SharedPreferences sharedPrefs;

    private boolean loggedIn;
    private String firebaseToken;
    private String username;

    public UserManager(Context context) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        loggedIn = sharedPrefs.getBoolean(KEY_LOGGED_IN, false);
        firebaseToken = sharedPrefs.getString(KEY_FIREBASE_TOKEN, null);
        username = sharedPrefs.getString(KEY_USERNAME, null);
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getUsername() {
        return username;
    }

    public void setLoggedIn(String username, String firebaseToken) {
        this.loggedIn = true;
        this.firebaseToken = firebaseToken;
        this.username = username;
        persistState();
    }

    public void setLoggedOut() {
        this.loggedIn = false;
        this.firebaseToken = null;
        this.username = null;
        persistState();
    }

    private void persistState() {
        sharedPrefs.edit()
                .putBoolean(KEY_LOGGED_IN, loggedIn)
                .putString(KEY_FIREBASE_TOKEN, firebaseToken)
                .putString(KEY_USERNAME, username)
                .apply();
    }

}
