package com.aluxian.codementor.services;

import android.content.SharedPreferences;

public class UserManager {

    private static final String KEY_USERNAME = "um_username";
    public static String LOGGED_IN_USERNAME;

    private SharedPreferences sharedPrefs;

    public UserManager(SharedPreferences sharedPrefs) {
        this.sharedPrefs = sharedPrefs;
        restoreState();
    }

    public boolean isLoggedIn() {
        return LOGGED_IN_USERNAME != null;
    }

    public void setLoggedIn(String username) {
        LOGGED_IN_USERNAME = username;
        persistState();
    }

    public void setLoggedOut() {
        LOGGED_IN_USERNAME = null;
        persistState();
    }

    private void restoreState() {
        LOGGED_IN_USERNAME = sharedPrefs.getString(KEY_USERNAME, null);
    }

    private void persistState() {
        sharedPrefs.edit().putString(KEY_USERNAME, LOGGED_IN_USERNAME).apply();
    }

}
