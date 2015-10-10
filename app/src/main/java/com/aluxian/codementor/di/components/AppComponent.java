package com.aluxian.codementor.di.components;

import com.aluxian.codementor.activities.BaseActivity;
import com.aluxian.codementor.di.modules.AppModule;
import com.aluxian.codementor.di.modules.HttpModule;
import com.aluxian.codementor.lib.PersistentCookieStore;
import com.aluxian.codementor.utils.UserManager;
import com.firebase.client.Firebase;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, HttpModule.class})
public interface AppComponent {

    void inject(BaseActivity baseActivity);

    Firebase firebaseRef();

    UserManager userManager();

    PersistentCookieStore cookieStore();

    OkHttpClient okHttpClient();

}
