package com.aluxian.codementor.di.components;

import com.aluxian.codementor.activities.BaseActivity;
import com.aluxian.codementor.di.annotations.PerActivity;
import com.aluxian.codementor.di.modules.ActivityModule;

import dagger.Component;

@PerActivity
@Component(dependencies = {AppComponent.class}, modules = {ActivityModule.class})
public interface ActivityComponent {

    BaseActivity activity();

}
