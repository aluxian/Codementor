package com.aluxian.codementor.presentation.presenters;

import android.text.TextUtils;

import com.aluxian.codementor.CoreServices;
import com.aluxian.codementor.R;
import com.aluxian.codementor.data.tasks.CodementorTasks;
import com.aluxian.codementor.data.tasks.FirebaseTasks;
import com.aluxian.codementor.data.tasks.TaskContinuations;
import com.aluxian.codementor.presentation.views.LoginActivityView;
import com.aluxian.codementor.utils.UserManager;

import java.util.concurrent.CancellationException;

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
        getView().showProgressDialog();
        getView().updateProgressDialogMessage(R.string.auth_step_codementor_connect);

        codementorTasks.extractAuthCode()
                .onSuccess(task -> {
                    if (cancelled) {
                        throw new CancellationException();
                    }

                    getView().updateProgressDialogMessage(R.string.auth_step_codementor_auth);
                    return task.getResult();
                })
                .onSuccessTask(task -> codementorTasks.signIn(username, password, task.getResult()))
                .onSuccess(task -> {
                    if (cancelled) {
                        throw new CancellationException();
                    }

                    getView().updateProgressDialogMessage(R.string.auth_step_firebase_extract);
                    return task.getResult();
                })
                .onSuccessTask(task -> codementorTasks.extractToken())
                .onSuccess(task -> {
                    if (cancelled) {
                        throw new CancellationException();
                    }

                    getView().updateProgressDialogMessage(R.string.auth_step_firebase_auth);
                    return task.getResult();
                })
                .onSuccessTask(task -> firebaseTasks.authenticate(task.getResult(), false))
                .onSuccess(task -> {
                    if (!task.isCancelled()) {
                        userManager.setLoggedIn(username, task.getResult().getToken());
                        getView().navigateToMainActivity();
                    }

                    return null;
                })
                .continueWith(task -> taskContinuations.logAndToastError())
                .continueWith(task -> {
                    getView().dismissProgressDialog();
                    return null;
                });

    }

    public void dialogCancelled() {
        cancelled = true;
    }

}
