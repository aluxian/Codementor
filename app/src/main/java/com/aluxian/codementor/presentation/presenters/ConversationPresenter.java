package com.aluxian.codementor.presentation.presenters;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.aluxian.codementor.data.events.NewMessageEvent;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.FirebaseMessage;
import com.aluxian.codementor.data.models.Message;
import com.aluxian.codementor.data.tasks.CodementorTasks;
import com.aluxian.codementor.data.tasks.FirebaseTasks;
import com.aluxian.codementor.data.tasks.ServerApiTasks;
import com.aluxian.codementor.data.tasks.TaskContinuations;
import com.aluxian.codementor.data.types.MessageType;
import com.aluxian.codementor.data.types.PresenceType;
import com.aluxian.codementor.presentation.adapters.ConversationAdapter;
import com.aluxian.codementor.presentation.views.ConversationView;
import com.aluxian.codementor.services.CoreServices;
import com.aluxian.codementor.services.ErrorHandler;
import com.aluxian.codementor.services.UserManager;
import com.aluxian.codementor.utils.Constants;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.squareup.otto.Bus;

import java.util.List;

import bolts.Task;

import static bolts.Task.UI_THREAD_EXECUTOR;

public class ConversationPresenter extends Presenter<ConversationView> {

    private static final String ORDER_BY_CREATED_AT = "created_at";
    private static final int ITEMS_BATCH_SIZE = 50;

    private @Nullable Task firebaseReAuthTask;

    private Bus bus;
    private ErrorHandler errorHandler;
    private UserManager userManager;
    private Firebase firebaseRef;

    private CodementorTasks codementorTasks;
    private FirebaseTasks firebaseTasks;
    private ServerApiTasks serverApiTasks;
    private TaskContinuations taskContinuations;

    private Chatroom chatroom;
    private ConversationAdapter conversationAdapter;

    private Firebase presenceRef;
    private Query messagesRef;

    public ConversationPresenter(ConversationView baseView, CoreServices coreServices,
                                 Chatroom chatroom, ConversationAdapter conversationAdapter) {
        super(baseView);

        bus = coreServices.getBus();
        errorHandler = coreServices.getErrorHandler();
        userManager = coreServices.getUserManager();
        firebaseRef = coreServices.getFirebaseRef();

        codementorTasks = coreServices.getCodementorTasks();
        firebaseTasks = coreServices.getFirebaseTasks();
        serverApiTasks = coreServices.getServerApiTasks();
        taskContinuations = coreServices.getTaskContinuations();

        presenceRef = firebaseRef.child(Constants.getPresencePath(chatroom.getOtherUser().getUsername()));
        messagesRef = firebaseRef.child(chatroom.getFirebasePath())
                .orderByChild(ORDER_BY_CREATED_AT)
                .limitToLast(ITEMS_BATCH_SIZE);

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

    public void loadMore() {
        getView().setRefreshing(true);
        firebaseRef.child(chatroom.getFirebasePath())
                .orderByChild(ORDER_BY_CREATED_AT)
                .limitToLast(ITEMS_BATCH_SIZE)
                .endAt(conversationAdapter.getOldestMessage().getCreatedAt() - 1)
                .addListenerForSingleValueEvent(olderMessagesEventListener);
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
                MessageType.MESSAGE,
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

        PresenceType presenceType;
        try {
            presenceType = PresenceType.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            presenceType = PresenceType.OFFLINE;
        }

        int resId = presenceType.statusResId;
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

                        conversationAdapter.addMessages(messages);
                        getView().showEmptyState(conversationAdapter.getItemCount() == 0);

                        return null;
                    }, UI_THREAD_EXECUTOR)
                    .continueWith(taskContinuations.logAndToastError(), UI_THREAD_EXECUTOR)
                    .continueWith(task -> {
                        getView().setRefreshing(false);
                        return null;
                    }, UI_THREAD_EXECUTOR);
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            handleFirebaseError(firebaseError);
        }
    };

    private ValueEventListener olderMessagesEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            firebaseTasks.parseMessagesSnapshot(dataSnapshot)
                    .onSuccess(task -> {
                        List<Message> messages = task.getResult();

                        if (messages.size() > 0) {
                            conversationAdapter.addMessages(messages);
                        } else {
                            getView().setAllMessagesLoaded(true);
                        }

                        return null;
                    }, UI_THREAD_EXECUTOR)
                    .continueWith(taskContinuations.logAndToastError(), UI_THREAD_EXECUTOR)
                    .continueWith(task -> {
                        getView().setRefreshing(false);
                        return null;
                    }, UI_THREAD_EXECUTOR);
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            handleFirebaseError(firebaseError);
        }
    };

}
