package com.aluxian.codementor.data.tasks;

import com.aluxian.codementor.utils.ErrorHandler;

import bolts.Continuation;

public class TaskContinuations {

    private ErrorHandler errorHandler;

    public TaskContinuations(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * A simple task which logs the error and shows a toast (if it exists).
     */
    public Continuation<Object, Void> logAndToastError() {
        return task -> {
            if (task.isFaulted()) {
                errorHandler.logAndToast(task.getError());
            }

            return null;
        };
    }

}
