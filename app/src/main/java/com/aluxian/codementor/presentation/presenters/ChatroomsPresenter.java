package com.aluxian.codementor.presentation.presenters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;

import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.ChatroomsList;
import com.aluxian.codementor.data.models.Message;
import com.aluxian.codementor.presentation.adapters.ChatroomsAdapter;
import com.aluxian.codementor.presentation.views.ChatroomsView;
import com.aluxian.codementor.services.CoreServices;
import com.aluxian.codementor.services.ErrorHandler;
import com.aluxian.codementor.services.UserManager;
import com.aluxian.codementor.tasks.CodementorTasks;

import bolts.Task;

import static com.aluxian.codementor.utils.Constants.UI;

public class ChatroomsPresenter extends Presenter<ChatroomsView> implements OnRefreshListener {

    public static final String ACTION_NEW_MESSAGE = "codementor.new_message";
    public static final String EXTRA_CHATROOM = "chatroom";
    public static final String EXTRA_MESSAGE = "message";

    private LocalBroadcastManager localBroadcastManager;
    private CodementorTasks codementorTasks;
    private ErrorHandler errorHandler;
    private UserManager userManager;

    private Task chatroomsListTask;
    private ChatroomsAdapter chatroomsAdapter;

    public ChatroomsPresenter(ChatroomsView baseView, ChatroomsAdapter chatroomsAdapter, CoreServices coreServices) {
        super(baseView);
        this.chatroomsAdapter = chatroomsAdapter;

        localBroadcastManager = coreServices.getLocalBroadcastManager();
        codementorTasks = coreServices.getCodementorTasks();
        errorHandler = coreServices.getErrorHandler();
        userManager = coreServices.getUserManager();
    }

    @Override
    public void start() {
        super.start();
        localBroadcastManager.registerReceiver(newMessageReceiver, new IntentFilter(ACTION_NEW_MESSAGE));
        onRefresh();
    }

    @Override
    public void stop() {
        super.stop();
        localBroadcastManager.unregisterReceiver(newMessageReceiver);
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

    private BroadcastReceiver newMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Chatroom chatroom = (Chatroom) intent.getSerializableExtra(EXTRA_CHATROOM);
            Message message = (Message) intent.getSerializableExtra(EXTRA_MESSAGE);

            if (chatroomsAdapter.isNewestChatroom(chatroom)) {
                chatroom.setContentDescription(message.getContentDescription());
                chatroomsAdapter.replaceFirstChatroom(chatroom);
                getView().scrollToTop();
            } else {
                onRefresh();
            }
        }
    };

}
