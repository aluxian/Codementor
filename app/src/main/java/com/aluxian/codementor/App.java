package com.aluxian.codementor;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.firebase.client.Firebase;

import io.fabric.sdk.android.Fabric;

public class App extends Application {

    private CoreServices coreServices;

    @Override
    public void onCreate() {
        super.onCreate();

        Fresco.initialize(this);
        Firebase.setAndroidContext(this);
        Fabric.with(this, new Crashlytics());

        coreServices = new CoreServices(this);
    }

    public CoreServices getCoreServices() {
        return coreServices;
    }

}
