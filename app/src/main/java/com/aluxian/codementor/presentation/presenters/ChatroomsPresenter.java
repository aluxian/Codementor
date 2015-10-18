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
    public void create() {
        super.create();
        bus.register(this);
    }

    @Override
    public void resume() {
        super.resume();
        getView().setRefreshing(true);
        onRefresh();
    }

    @Override
    public void destroy() {
//        bus.unregister(this); TODO
//        Caused by: java.lang.IllegalArgumentException: Missing event handler for an annotated method.
// Is class com.aluxian.codementor.presentation.presenters.ChatroomsPresenter registered?
//        at com.squareup.otto.Bus.unregister(Bus.java:290)
//        at com.aluxian.codementor.presentation.presenters.ChatroomsPresenter.destroy(ChatroomsPresenter.java:52)
//        at com.aluxian.codementor.presentation.fragments.BaseFragment.onDestroy(BaseFragment.java:46)
//        at android.support.v4.app.Fragment.performDestroy(Fragment.java:2182)
//        at android.support.v4.app.FragmentManagerImpl.moveToState(FragmentManager.java:1157)
//        at android.support.v4.app.FragmentManagerImpl.moveToState(FragmentManager.java:1207)
//        at android.support.v4.app.FragmentManagerImpl.moveToState(FragmentManager.java:1189)
//        at android.support.v4.app.FragmentManagerImpl.dispatchDestroy(FragmentManager.java:2038)
//        at android.support.v4.app.FragmentController.dispatchDestroy(FragmentController.java:235)
//        at android.support.v4.app.FragmentActivity.onDestroy(FragmentActivity.java:290)
//        at android.support.v7.app.AppCompatActivity.onDestroy(AppCompatActivity.java:161)
//        at com.aluxian.codementor.presentation.activities.BaseActivity.onDestroy(BaseActivity.java:38)
//        at android.app.Activity.performDestroy(Activity.java:6169)
    }

    @Subscribe
    public void newMessageReceived(NewMessageEvent event) {
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
                })
                .continueWith(task -> taskContinuations.logAndToastError());
    }

}
