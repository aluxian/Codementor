package com.aluxian.codementor.presentation.presenters;

import com.aluxian.codementor.data.types.PresenceType;
import com.aluxian.codementor.presentation.views.MainActivityView;
import com.aluxian.codementor.services.CoreServices;
import com.aluxian.codementor.services.ErrorHandler;
import com.aluxian.codementor.services.UserManager;
import com.aluxian.codementor.tasks.FirebaseTasks;

import java.util.Arrays;

import static com.aluxian.codementor.utils.Constants.UI;

public class MainActivityPresenter extends Presenter<MainActivityView> {

    private UserManager userManager;
    private FirebaseTasks firebaseTasks;
    private ErrorHandler errorHandler;

    public MainActivityPresenter(MainActivityView baseView, CoreServices coreServices) {
        super(baseView);

        userManager = coreServices.getUserManager();
        firebaseTasks = coreServices.getFirebaseTasks();
        errorHandler = coreServices.getErrorHandler();
    }

    @Override
    public void resume() {
        super.resume();
        setStatus(PresenceType.ONLINE, PresenceType.OFFLINE, PresenceType.AWAY);
    }

    @Override
    public void destroy() {
        super.destroy();
        setStatus(PresenceType.OFFLINE, PresenceType.ONLINE);
    }

    private void setStatus(PresenceType newPresenceType, PresenceType... requiredPresenceType) {
        String username = userManager.getUsername();
        firebaseTasks.getPresence(username)
                .onSuccessTask(task -> {
                    if (Arrays.asList(requiredPresenceType).contains(task.getResult())) {
                        return firebaseTasks.setPresence(username, newPresenceType);
                    }

                    return null;
                }, UI)
                .continueWith(errorHandler::logAndToastTask, UI);

    }

}
