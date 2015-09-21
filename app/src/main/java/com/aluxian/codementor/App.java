package com.aluxian.codementor;

import android.app.Application;

import com.aluxian.codementor.lib.PersistentCookieStore;
import com.aluxian.codementor.utils.UserManager;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.firebase.client.Firebase;
import com.squareup.okhttp.OkHttpClient;

import java.net.CookieManager;
import java.net.CookiePolicy;

public class App extends Application {

    private UserManager userManager;
    private OkHttpClient okHttpClient;
    private PersistentCookieStore cookieStore;
    private Runnable newMessageCallback;

    @Override
    public void onCreate() {
        super.onCreate();

        Fresco.initialize(this);
        Firebase.setAndroidContext(this);

        userManager = new UserManager(this);
        cookieStore = new PersistentCookieStore(this);

        okHttpClient = new OkHttpClient();
        okHttpClient.setCookieHandler(new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL));
        okHttpClient.setFollowRedirects(false);
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public PersistentCookieStore getCookieStore() {
        return cookieStore;
    }

    public Runnable getNewMessageCallback() {
        return newMessageCallback;
    }

    public void setNewMessageCallback(Runnable newMessageCallback) {
        this.newMessageCallback = newMessageCallback;
    }

}
