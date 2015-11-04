package com.aluxian.codementor.presentation.listeners;

import com.aluxian.codementor.services.CoreServices;
import com.aluxian.codementor.services.ErrorHandler;
import com.aluxian.codementor.tasks.CodementorTasks;
import com.aluxian.codementor.tasks.FirebaseTasks;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import bolts.Task;

import static com.aluxian.codementor.utils.Constants.UI;

public abstract class QueryEventListener implements ValueEventListener {

    private CodementorTasks codementorTasks;
    private FirebaseTasks firebaseTasks;
    private ErrorHandler errorHandler;
    private Firebase firebaseRef;

    private Task firebaseReAuthTask;
    private Query query;

    public QueryEventListener(CoreServices coreServices) {
        codementorTasks = coreServices.getCodementorTasks();
        firebaseTasks = coreServices.getFirebaseTasks();
        errorHandler = coreServices.getErrorHandler();
        firebaseRef = coreServices.getFirebaseRef();
    }

    public void start() {
        set(getQuery());
    }

    public void stop() {
        unset(getQuery());
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        if (firebaseError.getCode() != FirebaseError.PERMISSION_DENIED) {
            onError(firebaseError);
            return;
        }

        if (firebaseReAuthTask != null) {
            return;
        }

        firebaseReAuthTask = codementorTasks.extractToken()
                .onSuccessTask(task -> firebaseTasks.authenticate(task.getResult(), true))
                .onSuccess(this::onReAuthSuccessful, UI)
                .continueWith(this::onReAuthCompleted, UI);
    }

    protected void onError(FirebaseError firebaseError) {
        onError(firebaseError.toException());
    }

    protected void onError(Exception exception) {
        errorHandler.logAndToast(exception);
    }

    private Void onReAuthSuccessful(Task task) {
        unset(getQuery());
        set(getQuery());
        return null;
    }

    private Void onReAuthCompleted(Task task) {
        if (task.isFaulted()) {
            onError(task.getError());
        }

        return null;
    }

    protected abstract Query createQuery(Firebase firebase);

    protected abstract void set(Query query);

    protected abstract void unset(Query query);

    private Query getQuery() {
        if (query == null) {
            query = createQuery(firebaseRef);
        }

        return query;
    }

}
