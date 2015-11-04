package com.aluxian.codementor.presentation.presenters;

import com.aluxian.codementor.data.models.User;
import com.aluxian.codementor.data.types.PresenceType;
import com.aluxian.codementor.presentation.views.MainActivityView;
import com.aluxian.codementor.services.CoreServices;
import com.aluxian.codementor.services.ErrorHandler;
import com.aluxian.codementor.services.UserManager;
import com.aluxian.codementor.tasks.FirebaseTasks;

import java.util.Arrays;

import static com.aluxian.codementor.utils.Constants.UI;

public class MainActivityPresenter extends Presenter<MainActivityView> {

    private FirebaseTasks firebaseTasks;

    public MainActivityPresenter(MainActivityView baseView, CoreServices coreServices) {
        super(baseView);
        firebaseTasks = coreServices.getFirebaseTasks();
    }

    @Override
    public void start() {
        super.start();
        setPresence(PresenceType.ONLINE, PresenceType.OFFLINE, PresenceType.AWAY);
    }

    @Override
    public void stop() {
        super.stop();
        setPresence(PresenceType.OFFLINE, PresenceType.ONLINE);
    }

    private void setPresence(PresenceType newPresenceType, PresenceType... requiredPresenceType) {
        User user = new User(UserManager.LOGGED_IN_USERNAME);
        firebaseTasks.getPresence(user)
                .onSuccessTask(task -> {
                    if (Arrays.asList(requiredPresenceType).contains(task.getResult())) {
                        return firebaseTasks.setPresence(user, newPresenceType);
                    }

                    return null;
                }, UI)
                .continueWith(ErrorHandler::logErrorTask, UI);

    }

}
