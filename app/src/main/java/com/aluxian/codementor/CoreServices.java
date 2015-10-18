package com.aluxian.codementor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aluxian.codementor.data.tasks.CodementorTasks;
import com.aluxian.codementor.data.tasks.FirebaseTasks;
import com.aluxian.codementor.data.tasks.ServerApiTasks;
import com.aluxian.codementor.data.tasks.TaskContinuations;
import com.aluxian.codementor.lib.PersistentCookieStore;
import com.aluxian.codementor.utils.ErrorHandler;
import com.aluxian.codementor.utils.UserManager;
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
    private ServerApiTasks serverApiTasks;
    private TaskContinuations taskContinuations;

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
        firebaseRef = new Firebase("https://codementor.firebaseio.com/");
        okHttpClient = new OkHttpClient();
        okHttpClient.setCookieHandler(new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL));
        okHttpClient.setFollowRedirects(false);

        // Tasks
        codementorTasks = new CodementorTasks(okHttpClient);
        firebaseTasks = new FirebaseTasks(firebaseRef, errorHandler, userManager);
        serverApiTasks = new ServerApiTasks(okHttpClient);
        taskContinuations = new TaskContinuations(errorHandler);
    }

    public Context getContext() {
        return context;
    }

    public SharedPreferences getSharedPrefs() {
        return sharedPrefs;
    }

    public PersistentCookieStore getCookieStore() {
        return cookieStore;
    }

    public Firebase getFirebaseRef() {
        return firebaseRef;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public CodementorTasks getCodementorTasks() {
        return codementorTasks;
    }

    public FirebaseTasks getFirebaseTasks() {
        return firebaseTasks;
    }

    public ServerApiTasks getServerApiTasks() {
        return serverApiTasks;
    }

    public TaskContinuations getTaskContinuations() {
        return taskContinuations;
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
