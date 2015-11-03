package com.aluxian.codementor.presentation.presenters;

import android.text.TextUtils;

import com.aluxian.codementor.R;
import com.aluxian.codementor.presentation.views.LoginActivityView;
import com.aluxian.codementor.services.CoreServices;
import com.aluxian.codementor.services.ErrorHandler;
import com.aluxian.codementor.services.UserManager;
import com.aluxian.codementor.tasks.CodementorTasks;
import com.aluxian.codementor.tasks.FirebaseTasks;
import com.aluxian.codementor.utils.PersistentCookieStore;
import com.firebase.client.AuthData;

import java.util.concurrent.CancellationException;

import bolts.Continuation;
import bolts.Task;

import static com.aluxian.codementor.utils.Constants.UI;

public class LoginActivityPresenter extends Presenter<LoginActivityView> {

    private FirebaseTasks firebaseTasks;
    private CodementorTasks codementorTasks;

    private ErrorHandler errorHandler;
    private PersistentCookieStore cookieStore;
    private UserManager userManager;

    private boolean cancelled;

    public LoginActivityPresenter(LoginActivityView baseView, CoreServices coreServices) {
        super(baseView);

        firebaseTasks = coreServices.getFirebaseTasks();
        codementorTasks = coreServices.getCodementorTasks();

        errorHandler = coreServices.getErrorHandler();
        cookieStore = coreServices.getCookieStore();
        userManager = coreServices.getUserManager();

        cookieStore.removeAll();
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
                .onSuccess(updateUnlessCancelled(0), UI)
                .onSuccessTask(task -> codementorTasks.signIn(username, password, task.getResult()))
                .onSuccess(updateUnlessCancelled(R.string.auth_step_firebase), UI)
                .onSuccessTask(task -> codementorTasks.extractToken())
                .onSuccess(updateUnlessCancelled(0), UI)
                .onSuccessTask(task -> firebaseTasks.authenticate(task.getResult(), false))
                .onSuccess(loggedIn(username), UI)
                .continueWith(this::onErrorHandler, UI)
                .continueWith(this::onDismissDialog, UI);

    }

    private <T> T onErrorHandler(Task<T> task) {
        if (task.isFaulted()) {
            dialogCancelled();
        }

        return errorHandler.logAndToastTask(task);
    }

    private Void onDismissDialog(Task task) {
        getView().dismissProgressDialog();
        return null;
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
                cookieStore.removeAll();
                throw new CancellationException();
            }

            if (stringResId != 0) {
                getView().updateProgressDialogMessage(stringResId);
            }

            return task.getResult();
        };
    }

    public void dialogCancelled() {
        cookieStore.removeAll();
        cancelled = true;
    }

}
