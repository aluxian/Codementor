package com.aluxian.codementor;

import android.app.Application;

import com.aluxian.codementor.di.components.AppComponent;
import com.aluxian.codementor.di.components.DaggerAppComponent;
import com.aluxian.codementor.di.modules.AppModule;
import com.aluxian.codementor.di.modules.HttpModule;
import com.aluxian.codementor.utils.UserManager;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.firebase.client.Firebase;
import com.squareup.okhttp.OkHttpClient;

public class App extends Application {

    private AppComponent appComponent;

    private Runnable newMessageCallback;

    @Override
    public void onCreate() {
        super.onCreate();

        Fresco.initialize(this);
        Firebase.setAndroidContext(this);

        this.initializeInjector();
    }

    private void initializeInjector() {
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .httpModule(new HttpModule(this))
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    public UserManager getUserManager() {
        return getAppComponent().userManager();
    }

    public OkHttpClient getOkHttpClient() {
        return appComponent.okHttpClient();
    }

    public Runnable getNewMessageCallback() {
        return newMessageCallback;
    }

    public void setNewMessageCallback(Runnable newMessageCallback) {
        this.newMessageCallback = newMessageCallback;
    }

    public Firebase getFirebaseRef() {
        return getAppComponent().firebaseRef();
    }

}
