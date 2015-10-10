package com.aluxian.codementor.di.modules;

import android.content.Context;

import com.aluxian.codementor.utils.UserManager;
import com.firebase.client.Firebase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private final Context context;

    public AppModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    Firebase provideFirebase() {
        return new Firebase("https://codementor.firebaseio.com/");
    }

    @Provides
    @Singleton
    UserManager provideUserManager() {
        return new UserManager(context);
    }

}
