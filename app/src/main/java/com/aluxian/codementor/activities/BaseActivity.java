package com.aluxian.codementor.activities;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.aluxian.codementor.App;
import com.aluxian.codementor.di.components.AppComponent;
import com.aluxian.codementor.di.modules.ActivityModule;

/**
 * Base {@link android.app.Activity} class for every Activity in this application.
 */
public abstract class BaseActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getAppComponent().inject(this);
//    }

    /**
     * Adds a {@link Fragment} to this activity's layout.
     *
     * @param containerViewId The container view where to add the fragment.
     * @param fragment        The fragment to be added.
     */
    protected void replaceFragment(int containerViewId, Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, fragment)
                .commit();
    }

    /**
     * Get the main Application component for dependency injection.
     */
    protected AppComponent getAppComponent() {
        return ((App) getApplication()).getAppComponent();
    }

    /**
     * Get an Activity module for dependency injection.
     */
    protected ActivityModule getActivityModule() {
        return new ActivityModule(this);
    }

}
