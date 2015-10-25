package com.aluxian.codementor.presentation.presenters;

import com.aluxian.codementor.CoreServices;
import com.aluxian.codementor.Presence;
import com.aluxian.codementor.data.tasks.FirebaseTasks;
import com.aluxian.codementor.data.tasks.TaskContinuations;
import com.aluxian.codementor.presentation.views.MainActivityView;
import com.aluxian.codementor.utils.UserManager;

import java.util.Arrays;

public class MainActivityPresenter extends Presenter<MainActivityView> {

    private UserManager userManager;
    private FirebaseTasks firebaseTasks;
    private TaskContinuations taskContinuations;

    public MainActivityPresenter(MainActivityView baseView, CoreServices coreServices) {
        super(baseView);

        userManager = coreServices.getUserManager();
        firebaseTasks = coreServices.getFirebaseTasks();
        taskContinuations = coreServices.getTaskContinuations();
    }

    @Override
    public void resume() {
        super.resume();
        setStatus(Presence.ONLINE, Presence.OFFLINE, Presence.AWAY);
    }

    @Override
    public void pause() {
        super.pause();
        setStatus(Presence.AWAY, Presence.ONLINE);
    }

    @Override
    public void destroy() {
        super.destroy();
        setStatus(Presence.OFFLINE, Presence.AWAY);
    }

    private void setStatus(Presence newPresence, Presence... requiredPresence) {
        String username = userManager.getUsername();
        firebaseTasks.getPresence(username)
                .onSuccessTask(task -> {
                    if (Arrays.asList(requiredPresence).contains(task.getResult())) {
                        return firebaseTasks.setPresence(username, newPresence);
                    }

                    return null;
                })
                .continueWith(taskContinuations.logAndToastError());

    }

}
