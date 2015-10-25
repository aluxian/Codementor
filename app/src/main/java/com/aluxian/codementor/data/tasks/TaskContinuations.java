package com.aluxian.codementor.data.tasks;

import com.aluxian.codementor.services.ErrorHandler;

import bolts.Continuation;

public class TaskContinuations {

    private ErrorHandler errorHandler;

    public TaskContinuations(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * A simple task which logs the error (if it exists).
     */
    public <T, C> Continuation<T, C> logError() {
        return task -> {
            if (task.isFaulted()) {
                errorHandler.log(task.getError());
            }

            return null;
        };
    }

    /**
     * A simple task which logs the error and shows a toast (if it exists).
     */
    public <T, C> Continuation<T, C> logAndToastError() {
        return task -> {
            if (task.isFaulted()) {
                errorHandler.logAndToast(task.getError());
            }

            return null;
        };
    }

}
