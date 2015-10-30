package com.aluxian.codementor.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aluxian.codementor.tasks.CodementorTasks;
import com.aluxian.codementor.tasks.FirebaseTasks;
import com.aluxian.codementor.utils.Constants;
import com.aluxian.codementor.utils.PersistentCookieStore;
import com.firebase.client.Firebase;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;

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
    private Bus bus;

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
        bus = new Bus();

        // Network
        firebaseRef = new Firebase(Constants.FIREBASE_URL);
        okHttpClient = new OkHttpClient();
        okHttpClient.setCookieHandler(new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL));
        okHttpClient.setFollowRedirects(false);

        // Tasks
        codementorTasks = new CodementorTasks(okHttpClient, userManager);
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

    public Bus getBus() {
        return bus;
    }

}
