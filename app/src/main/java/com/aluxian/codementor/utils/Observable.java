package com.aluxian.codementor.utils;

import java.util.ArrayList;
import java.util.List;

public class Observable<T> {

    private List<T> mListeners = new ArrayList<>();

    public void addListener(T listener) {
        mListeners.add(listener);
    }

    public void removeListener(T listener) {
        mListeners.remove(listener);
    }

    public void notifyListeners(Observable.Notification<T> notification) {
        //noinspection Convert2streamapi
        for (T listener : mListeners) {
            notification.run(listener);
        }
    }

    interface Notification<T> {

        void run(T listener);

    }

}
