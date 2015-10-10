package com.aluxian.codementor.di.modules;

import com.aluxian.codementor.activities.BaseActivity;
import com.aluxian.codementor.di.annotations.PerActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {

    private final BaseActivity activity;

    public ActivityModule(BaseActivity activity) {
        this.activity = activity;
    }

    @Provides
    @PerActivity
    BaseActivity activity() {
        return activity;
    }

}
