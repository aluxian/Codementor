package com.aluxian.codementor.presentation.presenters;

import android.text.TextUtils;

import com.aluxian.codementor.CoreServices;
import com.aluxian.codementor.R;
import com.aluxian.codementor.data.tasks.CodementorTasks;
import com.aluxian.codementor.data.tasks.FirebaseTasks;
import com.aluxian.codementor.data.tasks.TaskContinuations;
import com.aluxian.codementor.presentation.views.LoginActivityView;
import com.aluxian.codementor.utils.UserManager;
import com.firebase.client.AuthData;

import java.util.concurrent.CancellationException;

import bolts.Continuation;

import static bolts.Task.UI_THREAD_EXECUTOR;

public class LoginActivityPresenter extends Presenter<LoginActivityView> {

    private FirebaseTasks firebaseTasks;
    private CodementorTasks codementorTasks;
    private TaskContinuations taskContinuations;
    private UserManager userManager;
    private boolean cancelled;

    public LoginActivityPresenter(LoginActivityView baseView, CoreServices coreServices) {
        super(baseView);

        firebaseTasks = coreServices.getFirebaseTasks();
        codementorTasks = coreServices.getCodementorTasks();
        taskContinuations = coreServices.getTaskContinuations();
        userManager = coreServices.getUserManager();
    }

    public void logIn(String username, String password) {
        if (TextUtils.isEmpty(username)) {
            getView().setUsernameFieldError(R.string.error_field_required);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            getView().setPasswordFieldError(R.string.error_field_required);
            return;
        }

        cancelled = false;
        getView().showProgressDialog(R.string.auth_step_codementor);

        codementorTasks.extractAuthCode()
                .onSuccess(updateUnlessCancelled(0), UI_THREAD_EXECUTOR)
                .onSuccessTask(task -> codementorTasks.signIn(username, password, task.getResult()))
                .onSuccess(updateUnlessCancelled(R.string.auth_step_firebase), UI_THREAD_EXECUTOR)
                .onSuccessTask(task -> codementorTasks.extractToken())
                .onSuccess(updateUnlessCancelled(0), UI_THREAD_EXECUTOR)
                .onSuccessTask(task -> firebaseTasks.authenticate(task.getResult(), false))
                .onSuccess(loggedIn(username), UI_THREAD_EXECUTOR)
                .continueWith(taskContinuations.logAndToastError(), UI_THREAD_EXECUTOR)
                .continueWith(dismissDialog(), UI_THREAD_EXECUTOR);

    }

    private <T, C> Continuation<T, C> dismissDialog() {
        return task -> {
            getView().dismissProgressDialog();
            return null;
        };
    }

    private <T extends AuthData, C> Continuation<T, C> loggedIn(String username) {
        return task -> {
            if (!task.isCancelled()) {
                userManager.setLoggedIn(username, task.getResult().getToken());
                getView().navigateToMainActivity();
            }

            return null;
        };
    }

    private <T> Continuation<T, T> updateUnlessCancelled(int stringResId) {
        return task -> {
            if (cancelled) {
                throw new CancellationException();
            }

            if (stringResId != 0) {
                getView().updateProgressDialogMessage(stringResId);
            }

            return task.getResult();
        };
    }

    public void dialogCancelled() {
        cancelled = true;
    }

}
