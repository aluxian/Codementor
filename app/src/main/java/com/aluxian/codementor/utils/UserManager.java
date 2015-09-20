package com.aluxian.codementor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UserManager extends Observable<UserManager.Listener> {

    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_FIREBASE_TOKEN = "firebase_token";
    private static final String KEY_USERNAME = "username";

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

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public String getUsername() {
        return username;
    }

    public void setLoggedIn(String username, String firebaseToken) {
        this.loggedIn = true;
        this.firebaseToken = firebaseToken;
        this.username = username;

        persistState();
        notifyListeners(Listener::onLoggedIn);
    }

    public void setLoggedOut() {
        this.loggedIn = false;
        this.firebaseToken = null;
        this.username = null;

        persistState();
        notifyListeners(Listener::onLoggedOut);
    }

    private void persistState() {
        sharedPrefs.edit()
                .putBoolean(KEY_LOGGED_IN, loggedIn)
                .putString(KEY_FIREBASE_TOKEN, firebaseToken)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    /**
     * Callback interface for events related to UserManager.
     */
    public interface Listener {

        /**
         * Called when the user logs in.
         */
        void onLoggedIn();

        /**
         * Called when the user logs out.
         */
        void onLoggedOut();

    }

}
