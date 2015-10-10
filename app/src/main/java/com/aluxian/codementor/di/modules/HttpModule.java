package com.aluxian.codementor.di.modules;

import android.content.Context;

import com.aluxian.codementor.lib.PersistentCookieStore;
import com.squareup.okhttp.OkHttpClient;

import java.net.CookieManager;
import java.net.CookiePolicy;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class HttpModule {

    private final Context context;

    public HttpModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    PersistentCookieStore provideCookieStore() {
        return new PersistentCookieStore(context);
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(PersistentCookieStore cookieStore) {
        CookieManager cookieManager = new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL);

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setCookieHandler(cookieManager);
        okHttpClient.setFollowRedirects(false);

        return okHttpClient;
    }

}
