package com.aluxian.codementor;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.firebase.client.Firebase;

public class App extends Application {

    private CoreServices coreServices;

    @Override
    public void onCreate() {
        super.onCreate();

        Fresco.initialize(this);
        Firebase.setAndroidContext(this);

        coreServices = new CoreServices(this);
    }

    public CoreServices getCoreServices() {
        return coreServices;
    }

}
