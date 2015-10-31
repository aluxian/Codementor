package com.aluxian.codementor.presentation.presenters;

import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;

import com.aluxian.codementor.data.events.NewMessageEvent;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.ChatroomsList;
import com.aluxian.codementor.presentation.adapters.ChatroomsAdapter;
import com.aluxian.codementor.presentation.views.ChatroomsView;
import com.aluxian.codementor.services.CoreServices;
import com.aluxian.codementor.services.ErrorHandler;
import com.aluxian.codementor.services.UserManager;
import com.aluxian.codementor.tasks.CodementorTasks;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import bolts.Task;

import static com.aluxian.codementor.utils.Constants.UI;

public class ChatroomsPresenter extends Presenter<ChatroomsView> implements OnRefreshListener {

    private Bus bus;
    private CodementorTasks codementorTasks;
    private ErrorHandler errorHandler;
    private UserManager userManager;

    private Task chatroomsListTask;
    private ChatroomsAdapter chatroomsAdapter;

    public ChatroomsPresenter(ChatroomsView baseView, ChatroomsAdapter chatroomsAdapter, CoreServices coreServices) {
        super(baseView);
        this.chatroomsAdapter = chatroomsAdapter;

        bus = coreServices.getBus();
        codementorTasks = coreServices.getCodementorTasks();
        errorHandler = coreServices.getErrorHandler();
        userManager = coreServices.getUserManager();
    }

    @Override
    public void viewReady() {
        onRefresh();
    }

    @Override
    public void resume() {
        super.resume();
        bus.register(this);
    }

    @Override
    public void pause() {
        super.pause();
        bus.unregister(this);
    }

    @Subscribe
    public void newMessageReceived(NewMessageEvent event) {
        Chatroom chatroom = event.getChatroom();
        if (chatroomsAdapter.isNewestChatroom(chatroom)) {
            chatroom.updateTimestamp(event.getMessage().getTimestamp());
            chatroom.updateContentDescription(event.getMessage());
            chatroomsAdapter.updateChatroom(chatroom);
            getView().scrollToTop();
        } else {
            onRefresh();
        }
    }

    @Override
    public void onRefresh() {
        boolean isAlreadyLoading = chatroomsListTask != null && !chatroomsListTask.isCompleted();
        if (isAlreadyLoading || !userManager.isLoggedIn()) {
            return;
        }

        getView().setRefreshing(true);
        chatroomsListTask = codementorTasks.getChatroomsList()
                .onSuccess(this::onChatroomsList, UI)
                .continueWith(errorHandler::logAndToastTask, UI)
                .continueWith(this::onLoadingFinished, UI);
    }

    private Void onChatroomsList(Task<ChatroomsList> task) {
        chatroomsAdapter.addAll(task.getResult().getRecentChats());
        getView().showEmptyState(chatroomsAdapter.getItemCount() == 0);
        return null;
    }

    private Void onLoadingFinished(Task task) {
        getView().setRefreshing(false);
        getView().scrollToTop();
        return null;
    }

}
