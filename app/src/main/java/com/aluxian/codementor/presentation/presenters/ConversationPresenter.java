package com.aluxian.codementor.presentation.presenters;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.aluxian.codementor.Constants;
import com.aluxian.codementor.CoreServices;
import com.aluxian.codementor.Presence;
import com.aluxian.codementor.data.events.NewMessageEvent;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.FirebaseMessage;
import com.aluxian.codementor.data.models.Message;
import com.aluxian.codementor.data.tasks.CodementorTasks;
import com.aluxian.codementor.data.tasks.FirebaseTasks;
import com.aluxian.codementor.data.tasks.ServerApiTasks;
import com.aluxian.codementor.data.tasks.TaskContinuations;
import com.aluxian.codementor.presentation.adapters.ConversationAdapter;
import com.aluxian.codementor.presentation.views.ConversationView;
import com.aluxian.codementor.utils.ErrorHandler;
import com.aluxian.codementor.utils.UserManager;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.squareup.otto.Bus;

import java.util.List;

import bolts.Task;

import static bolts.Task.UI_THREAD_EXECUTOR;

public class ConversationPresenter extends Presenter<ConversationView> {

    private @Nullable Task firebaseReAuthTask;

    private Bus bus;
    private ErrorHandler errorHandler;
    private UserManager userManager;

    private CodementorTasks codementorTasks;
    private FirebaseTasks firebaseTasks;
    private ServerApiTasks serverApiTasks;
    private TaskContinuations taskContinuations;

    private Chatroom chatroom;
    private ConversationAdapter conversationAdapter;

    private Firebase presenceRef;
    private Firebase messagesRef;

    public ConversationPresenter(ConversationView baseView, CoreServices coreServices,
                                 Chatroom chatroom, ConversationAdapter conversationAdapter) {
        super(baseView);

        bus = coreServices.getBus();
        errorHandler = coreServices.getErrorHandler();
        userManager = coreServices.getUserManager();

        codementorTasks = coreServices.getCodementorTasks();
        firebaseTasks = coreServices.getFirebaseTasks();
        serverApiTasks = coreServices.getServerApiTasks();
        taskContinuations = coreServices.getTaskContinuations();

        String presencePath = Constants.getPresencePath(chatroom.getOtherUser().getUsername());
        presenceRef = coreServices.getFirebaseRef().child(presencePath);
        messagesRef = coreServices.getFirebaseRef().child(chatroom.getFirebasePath());

        this.chatroom = chatroom;
        this.conversationAdapter = conversationAdapter;
    }

    @Override
    public void resume() {
        super.resume();
        getView().setRefreshing(true);
        addFirebaseListeners();
    }

    @Override
    public void pause() {
        super.pause();
        getView().setRefreshing(false);
        removeFirebaseListeners();
    }

    private void addFirebaseListeners() {
        presenceRef.addValueEventListener(presenceEventListener);
        messagesRef.addValueEventListener(messagesEventListener);
    }

    private void removeFirebaseListeners() {
        presenceRef.removeEventListener(presenceEventListener);
        messagesRef.removeEventListener(messagesEventListener);
    }

    public void sendMessage(String messageBody) {
        if (TextUtils.isEmpty(messageBody)) {
            return;
        }

        getView().setMessageFieldText("");
        FirebaseMessage firebaseMessage = new FirebaseMessage(
                chatroom.getChatroomId(),
                Message.Type.MESSAGE,
                messageBody,
                null, chatroom.getCurrentUser(),
                chatroom.getOtherUser()
        );

        conversationAdapter.addSentMessage(new Message(firebaseMessage, errorHandler, userManager.getUsername()));
        firebaseTasks.sendMessage(firebaseMessage, chatroom)
                .onSuccessTask(task -> serverApiTasks.sendMessage(firebaseMessage, task.getResult()))
                .continueWith(taskContinuations.logAndToastError(), UI_THREAD_EXECUTOR);
    }

    private void updateStatus(@Nullable String status) {
        if (status == null) {
            getView().setSubtitle(null);
            return;
        }

        Presence presence;
        try {
            presence = Presence.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            presence = Presence.OFFLINE;
        }

        int resId = presence.status;
        getView().setSubtitle(resId);
    }

    private void handleFirebaseError(FirebaseError firebaseError) {
        if (firebaseError.getCode() == FirebaseError.PERMISSION_DENIED) {
            if (firebaseReAuthTask != null && !firebaseReAuthTask.isCompleted()) {
                return;
            }

            firebaseReAuthTask = codementorTasks.extractToken()
                    .onSuccessTask(task -> firebaseTasks.authenticate(task.getResult(), true))
                    .onSuccess(task -> {
                        removeFirebaseListeners();
                        addFirebaseListeners();
                        return null;
                    }, UI_THREAD_EXECUTOR)
                    .continueWith(task -> {
                        if (task.isFaulted()) {
                            errorHandler.logAndToast(firebaseError.toException());
                        }

                        return null;
                    }, UI_THREAD_EXECUTOR);
        } else {
            errorHandler.logAndToast(firebaseError.toException());
        }
    }

    private ValueEventListener presenceEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            updateStatus(dataSnapshot.getValue(String.class));
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            updateStatus(null);
            handleFirebaseError(firebaseError);
        }
    };

    private ValueEventListener messagesEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            firebaseTasks.parseMessagesSnapshot(dataSnapshot)
                    .onSuccess(task -> {
                        List<Message> messages = task.getResult();

                        if (messages.size() > 0) {
                            Message lastMessage = messages.get(messages.size() - 1);
                            bus.post(new NewMessageEvent(chatroom, lastMessage));

                            serverApiTasks.markConversationRead(chatroom)
                                    .continueWith(taskContinuations.logAndToastError(), UI_THREAD_EXECUTOR);
                        }

                        conversationAdapter.updateList(messages);
                        getView().showEmptyState(conversationAdapter.getItemCount() == 0);
                        getView().setRefreshing(false);

                        return null;
                    }, UI_THREAD_EXECUTOR)
                    .continueWith(taskContinuations.logAndToastError(), UI_THREAD_EXECUTOR);
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            handleFirebaseError(firebaseError);
        }
    };

}
