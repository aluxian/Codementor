package com.aluxian.codementor.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.aluxian.codementor.tasks.CodementorTasks;
import com.aluxian.codementor.tasks.FirebaseTasks;
import com.aluxian.codementor.utils.Constants;
import com.aluxian.codementor.utils.PersistentCookieStore;
import com.firebase.client.Firebase;
import com.squareup.okhttp.OkHttpClient;

import java.net.CookieManager;
import java.net.CookiePolicy;

public class CoreServices {

    // Storage
    private SharedPreferences sharedPrefs;
    private PersistentCookieStore cookieStore;

    // General
    private Context context;
    private UserManager userManager;
    private ErrorHandler errorHandler;
    private LocalBroadcastManager localBroadcastManager;

    // Network
    private Firebase firebaseRef;
    private OkHttpClient okHttpClient;

    // Tasks
    private CodementorTasks codementorTasks;
    private FirebaseTasks firebaseTasks;

    public CoreServices(Context context) {
        this.context = context;

        // Storage
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        cookieStore = new PersistentCookieStore(context);

        // General
        userManager = new UserManager(sharedPrefs);
        errorHandler = new ErrorHandler(context);
        localBroadcastManager = LocalBroadcastManager.getInstance(context);

        // Network
        firebaseRef = new Firebase(Constants.FIREBASE_URL);
        okHttpClient = new OkHttpClient();
        okHttpClient.setCookieHandler(new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL));

        // Tasks
        codementorTasks = new CodementorTasks(okHttpClient);
        firebaseTasks = new FirebaseTasks(firebaseRef, codementorTasks, userManager);
    }

    public Context getContext() {
        return context;
    }

    public PersistentCookieStore getCookieStore() {
        return cookieStore;
    }

    public Firebase getFirebaseRef() {
        return firebaseRef;
    }

    public CodementorTasks getCodementorTasks() {
        return codementorTasks;
    }

    public FirebaseTasks getFirebaseTasks() {
        return firebaseTasks;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public LocalBroadcastManager getLocalBroadcastManager() {
        return localBroadcastManager;
    }

}
