package com.aluxian.codementor.presentation.activities;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.aluxian.codementor.App;
import com.aluxian.codementor.CoreServices;
import com.aluxian.codementor.presentation.presenters.Presenter;

/**
 * Base class for every activity in this application.
 *
 * @param <P> The type of {@link Presenter} this activity will use.
 */
public abstract class BaseActivity<P extends Presenter> extends AppCompatActivity {

    private @Nullable P presenter;

    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (presenter != null) {
            presenter.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.destroy();
        }
    }

    /**
     * Set the {@link Presenter} this fragment will use.
     *
     * @param presenter The fragment to be set.
     */
    protected void setPresenter(@Nullable P presenter) {
        this.presenter = presenter;
    }

    /**
     * @return The {@link Presenter} of the fragment.
     */
    @SuppressWarnings("NullableProblems")
    protected P getPresenter() {
        return presenter;
    }

    /**
     * @return A {@link CoreServices} instance from this fragments {@link App}.
     */
    protected CoreServices getCoreServices() {
        return ((App) getApplication()).getCoreServices();
    }

}
