package com.aluxian.codementor;

import android.app.Application;
import android.os.StrictMode;

import com.aluxian.codementor.services.CoreServices;
import com.crashlytics.android.Crashlytics;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.firebase.client.Firebase;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import io.fabric.sdk.android.Fabric;

public class App extends Application {

    private CoreServices coreServices;
    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        getRefWatcher();

        Fresco.initialize(this);
        Firebase.setAndroidContext(this);
        Fabric.with(this, new Crashlytics());

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        }
    }

    public CoreServices getCoreServices() {
        if (coreServices == null) {
            coreServices = new CoreServices(this);
        }

        return coreServices;
    }

    public RefWatcher getRefWatcher() {
        if (refWatcher == null) {
            refWatcher = LeakCanary.install(this);
        }

        return refWatcher;
    }

}
