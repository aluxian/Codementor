package com.aluxian.codementor.presentation.presenters;

import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;

import com.aluxian.codementor.CoreServices;
import com.aluxian.codementor.data.events.NewMessageEvent;
import com.aluxian.codementor.data.tasks.ServerApiTasks;
import com.aluxian.codementor.data.tasks.TaskContinuations;
import com.aluxian.codementor.presentation.adapters.ChatroomsAdapter;
import com.aluxian.codementor.presentation.views.ChatroomsView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import bolts.Task;

import static bolts.Task.UI_THREAD_EXECUTOR;

public class ChatroomsPresenter extends Presenter<ChatroomsView> implements OnRefreshListener {

    private @Nullable Task chatroomsListTask;
    private ChatroomsAdapter chatroomsAdapter;

    private ServerApiTasks serverApiTasks;
    private TaskContinuations taskContinuations;
    private Bus bus;

    public ChatroomsPresenter(ChatroomsView baseView, CoreServices coreServices, ChatroomsAdapter chatroomsAdapter) {
        super(baseView);

        serverApiTasks = coreServices.getServerApiTasks();
        taskContinuations = coreServices.getTaskContinuations();
        bus = coreServices.getBus();

        this.chatroomsAdapter = chatroomsAdapter;
    }

    @Override
    public void resume() {
        super.resume();
        bus.register(this);
        getView().setRefreshing(true);
        onRefresh();
    }

    @Override
    public void pause() {
        super.pause();
        bus.unregister(this);
    }

    @Subscribe
    public void newMessageReceived(NewMessageEvent event) {
        getView().setRefreshing(true);
        onRefresh();
    }

    @Override
    public void onRefresh() {
        if (chatroomsListTask != null && !chatroomsListTask.isCompleted()) {
            return;
        }

        chatroomsListTask = serverApiTasks.getChatroomsList()
                .onSuccess(task -> {
                    chatroomsAdapter.updateList(task.getResult().getRecentChats());
                    getView().showEmptyState(chatroomsAdapter.getItemCount() == 0);
                    getView().setRefreshing(false);
                    return null;
                }, UI_THREAD_EXECUTOR)
                .continueWith(taskContinuations.logAndToastError(), UI_THREAD_EXECUTOR);
    }

}
