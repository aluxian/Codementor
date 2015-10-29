package com.aluxian.codementor.presentation.presenters;

import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.util.SortedList;

import com.aluxian.codementor.data.events.NewMessageEvent;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.ChatroomsList;
import com.aluxian.codementor.presentation.adapters.ChatroomsAdapter;
import com.aluxian.codementor.presentation.listeners.ChatroomSelectedListener;
import com.aluxian.codementor.presentation.views.ChatroomsView;
import com.aluxian.codementor.services.CoreServices;
import com.aluxian.codementor.services.ErrorHandler;
import com.aluxian.codementor.tasks.ServerApiTasks;
import com.aluxian.codementor.utils.SortedListCallback;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import bolts.Task;

import static com.aluxian.codementor.utils.Constants.UI;

public class ChatroomsPresenter extends Presenter<ChatroomsView>
        implements OnRefreshListener, ChatroomSelectedListener {

    private Bus bus;
    private ServerApiTasks serverApiTasks;
    private ErrorHandler errorHandler;

    private @Nullable Task chatroomsListTask;
    private SortedList<Chatroom> chatrooms;

    public ChatroomsPresenter(ChatroomsView baseView, CoreServices coreServices) {
        super(baseView);

        bus = coreServices.getBus();
        serverApiTasks = coreServices.getServerApiTasks();
        errorHandler = coreServices.getErrorHandler();

        initAdapter();
    }

    private void initAdapter() {
        ChatroomsAdapter adapter = new ChatroomsAdapter(this);
        getView().setAdapter(adapter);
        chatrooms = new SortedList<>(Chatroom.class, new SortedListCallback<>(adapter));
        adapter.setHasStableIds(true);
        adapter.setList(chatrooms);
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

    @Override
    public void onChatroomSelected(Chatroom chatroom) {
        getView().closeDrawer();
        getView().onChatroomSelected(chatroom);
    }

    @Subscribe
    public void newMessageReceived(NewMessageEvent event) {
        Chatroom chatroom = event.getChatroom();
        chatroom.updateContentDescription(event.getMessage());
        chatrooms.add(chatroom);
    }

    @Override
    public void onRefresh() {
        if (isAlreadyLoading()) {
            return;
        }

        getView().setRefreshing(true);
        chatroomsListTask = serverApiTasks.getChatroomsList()
                .onSuccess(this::onGotChatroomsList, UI)
                .continueWith(errorHandler::logAndToastTask, UI)
                .continueWith(this::onLoadingFinished, UI);
    }

    private boolean isAlreadyLoading() {
        return chatroomsListTask != null && !chatroomsListTask.isCompleted();
    }

    private Void onGotChatroomsList(Task<ChatroomsList> task) {
        chatrooms.addAll(task.getResult().getRecentChats());
        getView().showEmptyState(chatrooms.size() == 0);
        return null;
    }

    private Void onLoadingFinished(Task task) {
        getView().setRefreshing(false);
        return null;
    }

}
