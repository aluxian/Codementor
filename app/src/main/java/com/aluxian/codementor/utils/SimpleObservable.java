package com.aluxian.codementor.utils;

public interface SimpleObservable<T> {

    void setListener(T listener);

    void removeListener();

}
