package com.aluxian.codementor.presentation.presenters;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.aluxian.codementor.data.events.NewMessageEvent;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.FirebaseMessage;
import com.aluxian.codementor.data.models.Message;
import com.aluxian.codementor.data.models.MessageData;
import com.aluxian.codementor.data.types.MessageType;
import com.aluxian.codementor.data.types.PresenceType;
import com.aluxian.codementor.presentation.adapters.ConversationAdapter;
import com.aluxian.codementor.presentation.views.ConversationView;
import com.aluxian.codementor.services.CoreServices;
import com.aluxian.codementor.services.ErrorHandler;
import com.aluxian.codementor.services.UserManager;
import com.aluxian.codementor.tasks.CodementorTasks;
import com.aluxian.codementor.tasks.FirebaseTasks;
import com.aluxian.codementor.utils.Constants;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import bolts.Task;

import static com.aluxian.codementor.utils.Constants.UI;

public class ConversationPresenter extends Presenter<ConversationView> {

    private static final String CREATED_AT = "created_at";
    private static final int BATCH_SIZE_INITIAL = 25;
    private static final int BATCH_SIZE = 100;

    private Bus bus;
    private ErrorHandler errorHandler;
    private UserManager userManager;

    private CodementorTasks codementorTasks;
    private FirebaseTasks firebaseTasks;
    private Firebase firebaseRef;

    private Query presenceRef;
    private Query messagesRef;

    private Task firebaseReAuthTask;
    private ConversationAdapter conversationAdapter;
    private Chatroom chatroom;

    public ConversationPresenter(ConversationView baseView, ConversationAdapter conversationAdapter,
                                 Chatroom chatroom, CoreServices coreServices) {
        super(baseView);
        this.conversationAdapter = conversationAdapter;
        this.chatroom = chatroom;

        bus = coreServices.getBus();
        errorHandler = coreServices.getErrorHandler();
        userManager = coreServices.getUserManager();

        codementorTasks = coreServices.getCodementorTasks();
        firebaseTasks = coreServices.getFirebaseTasks();
        firebaseRef = coreServices.getFirebaseRef();

        presenceRef = firebaseRef.child(Constants.presencePath(chatroom.getOtherUser().getUsername()));
        messagesRef = firebaseRef.child(chatroom.getFirebasePath())
                .orderByChild(CREATED_AT).limitToLast(BATCH_SIZE_INITIAL);
    }

    @Override
    public void start() {
        super.start();
        getView().setRefreshing(true);
        addFirebaseListeners();
    }

    @Override
    public void stop() {
        super.stop();
        getView().setRefreshing(false);
        removeFirebaseListeners();
    }

    private void addFirebaseListeners() {
        presenceRef.addValueEventListener(presenceEventListener);
        messagesRef.addValueEventListener(newMessagesEventListener);
    }

    private void removeFirebaseListeners() {
        presenceRef.removeEventListener(presenceEventListener);
        messagesRef.removeEventListener(newMessagesEventListener);
    }

    public void loadMore() {
        if (conversationAdapter.getItemCount() == 0) {
            return;
        }

        getView().setRefreshing(true);
        firebaseRef.child(chatroom.getFirebasePath())
                .orderByChild(CREATED_AT)
                .limitToLast(BATCH_SIZE)
                .endAt(conversationAdapter.getOldestMessage().getTimestamp() - 1)
                .addListenerForSingleValueEvent(oldMessagesEventListener);
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

        Message message = new Message(firebaseMessage, userManager.getUsername());
        conversationAdapter.optimisticallyAddMessage(message);

        firebaseTasks.sendMessage(firebaseMessage, chatroom)
                .onSuccessTask(task -> codementorTasks.sendMessage(firebaseMessage, task.getResult()))
                .continueWith(errorHandler::logAndToastTask, UI);
    }

    private ValueEventListener presenceEventListener = new ManagedValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            updateStatus(dataSnapshot.getValue(String.class));
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            super.onCancelled(firebaseError);
            updateStatus(null);
        }

        private void updateStatus(@Nullable String status) {
            if (status == null) {
                getView().setSubtitle(null);
                return;
            }

            getView().setSubtitle(PresenceType.parse(status).statusResId);
        }

    };

    private ValueEventListener newMessagesEventListener = new MessageValueEventListener() {

        @Override
        protected void onMessages(List<Message> messages) {
            if (messages.size() > 0) {
                if (conversationAdapter.getItemCount() > 0) {
                    bus.post(new NewMessageEvent(new Chatroom(chatroom), messages.get(messages.size() - 1)));
                }

                conversationAdapter.addNewMessages(messages);
                codementorTasks.markConversationRead(chatroom).continueWith(errorHandler::logAndToastTask, UI);
            }

            if (messages.size() < BATCH_SIZE_INITIAL) {
                getView().setAllMessagesLoaded(true);
            }

            getView().showEmptyState(conversationAdapter.getItemCount() == 0);
        }

    };

    private ValueEventListener oldMessagesEventListener = new MessageValueEventListener() {

        @Override
        protected void onMessages(List<Message> messages) {
            conversationAdapter.addOldMessages(messages);
            if (messages.size() < BATCH_SIZE) {
                getView().setAllMessagesLoaded(true);
            }
        }

    };

    private abstract class MessageValueEventListener extends ManagedValueEventListener {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Task.callInBackground(() -> parse(dataSnapshot))
                    .continueWith(task -> {
                        onMessages(task.getResult());
                        getView().setRefreshing(false);
                        return null;
                    }, UI);
        }

        private List<Message> parse(DataSnapshot snapshot) {
            List<Message> messages = new ArrayList<>();

            for (DataSnapshot child : snapshot.getChildren()) {
                MessageData messageData = child.getValue(MessageData.class);
                messages.add(new Message(messageData, userManager.getUsername()));
            }

            return messages;
        }

        protected abstract void onMessages(List<Message> messages);

    }

    private abstract class ManagedValueEventListener implements ValueEventListener {

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            if (firebaseError.getCode() != FirebaseError.PERMISSION_DENIED) {
                errorHandler.logAndToast(firebaseError.toException());
                return;
            }

            // Skip if already running
            if (firebaseReAuthTask != null && !firebaseReAuthTask.isCompleted()) {
                return;
            }

            firebaseReAuthTask = codementorTasks.extractToken()
                    .onSuccessTask(task -> firebaseTasks.authenticate(task.getResult(), true))
                    .onSuccess(this::onReAuthSuccessful, UI)
                    .continueWith(this::onReAuthCompleted, UI);
        }

        private Void onReAuthSuccessful(Task task) {
            removeFirebaseListeners();
            addFirebaseListeners();
            return null;
        }

        private Void onReAuthCompleted(Task task) {
            if (task.isFaulted()) {
                errorHandler.logAndToast(task.getError());
            }

            return null;
        }

    }

}
