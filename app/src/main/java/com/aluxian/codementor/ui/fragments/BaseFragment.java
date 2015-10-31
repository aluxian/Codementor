package com.aluxian.codementor.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.aluxian.codementor.App;
import com.aluxian.codementor.presentation.presenters.Presenter;
import com.aluxian.codementor.services.CoreServices;

/**
 * Base class for every fragment in this application.
 *
 * @param <P> The type of {@link Presenter} this fragment will use.
 */
public abstract class BaseFragment<P extends Presenter> extends Fragment {

    private @Nullable P presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (presenter != null) {
            view.post(presenter::viewReady);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.start();
        }
    }

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
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
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
        return ((App) getActivity().getApplication()).getCoreServices();
    }

}
