package com.aluxian.codementor;

import android.app.Application;
import android.os.StrictMode;

import com.aluxian.codementor.services.CoreServices;
import com.crashlytics.android.Crashlytics;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.firebase.client.Firebase;

import io.fabric.sdk.android.Fabric;

public class App extends Application {

    private CoreServices coreServices;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        }

        Fresco.initialize(this);
        Firebase.setAndroidContext(this);
        Fabric.with(this, new Crashlytics());

        coreServices = new CoreServices(this);
    }

    public CoreServices getCoreServices() {
        return coreServices;
    }

}
