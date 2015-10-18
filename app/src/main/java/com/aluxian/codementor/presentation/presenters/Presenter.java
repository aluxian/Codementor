package com.aluxian.codementor.presentation.presenters;

import com.aluxian.codementor.presentation.views.BaseView;

/**
 * Class representing a {@link Presenter} in a model-view-presenter (MVP) pattern.
 *
 * @param <T> The type of the view the {@link Presenter} is going to be responsible for.
 */
public abstract class Presenter<T extends BaseView> {

    private T baseView;

    /**
     * @param baseView The view to attach to the {@link Presenter}.
     */
    public Presenter(T baseView) {
        this.baseView = baseView;
    }

    /**
     * @return The view of this {@link Presenter}.
     */
    protected T getView() {
        return baseView;
    }

    /**
     * Method that controls the lifecycle of the view. It should be called in the view's onResume() method.
     */
    public void resume() {}

    /**
     * Method that controls the lifecycle of the view. It should be called in the view's onPause() method.
     */
    public void pause() {}

    /**
     * Method that controls the lifecycle of the view. It should be called in the view's onDestroy() method.
     */
    public void destroy() {}

}
