package com.aluxian.codementor.presentation.presenters;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

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
import com.aluxian.codementor.utils.QueryEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;

import java.util.TreeSet;

import bolts.Task;

import static com.aluxian.codementor.utils.Constants.UI;

public class ConversationPresenter extends Presenter<ConversationView> {

    private static final String CREATED_AT = "created_at";
    private static final int BATCH_SIZE_INITIAL = 25;
    private static final int BATCH_SIZE = 100;

    private LocalBroadcastManager localBroadcastManager;
    private ErrorHandler errorHandler;
    private UserManager userManager;

    private CodementorTasks codementorTasks;
    private FirebaseTasks firebaseTasks;
    private CoreServices coreServices;

    private QueryEventListener presenceListener;
    private QueryEventListener messagesListener;

    private ConversationAdapter conversationAdapter;
    private Chatroom chatroom;

    public ConversationPresenter(ConversationView baseView, ConversationAdapter conversationAdapter,
                                 Chatroom chatroom, CoreServices coreServices) {
        super(baseView);

        this.coreServices = coreServices;
        this.conversationAdapter = conversationAdapter;
        this.chatroom = chatroom;

        localBroadcastManager = coreServices.getLocalBroadcastManager();
        errorHandler = coreServices.getErrorHandler();
        userManager = coreServices.getUserManager();

        codementorTasks = coreServices.getCodementorTasks();
        firebaseTasks = coreServices.getFirebaseTasks();

        presenceListener = new PresenceEventListener(coreServices);
        messagesListener = new NewMessagesEventListener(coreServices);
    }

    @Override
    public void start() {
        super.start();
        getView().setRefreshing(true);
        presenceListener.start();
        messagesListener.start();
    }

    @Override
    public void stop() {
        super.stop();
        getView().setRefreshing(false);
        presenceListener.stop();
        messagesListener.stop();
    }

    public void loadMore() {
        if (conversationAdapter.getItemCount() == 0) {
            return;
        }

        getView().setRefreshing(true);
        new OldMessagesEventListener(coreServices).start();
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

        firebaseTasks.sendMessage(firebaseMessage, chatroom)
                .onSuccessTask(task -> codementorTasks.sendMessage(firebaseMessage, task.getResult()))
                .continueWith(errorHandler::logAndToastTask, UI);
    }

    private class PresenceEventListener extends QueryEventListener {

        public PresenceEventListener(CoreServices coreServices) {
            super(coreServices);
        }

        @Override
        protected Query createQuery(Firebase firebase) {
            return firebase.child(chatroom.getOtherUser().getPresencePath());
        }

        @Override
        protected void set(Query query) {
            query.addValueEventListener(this);
        }

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

    }

    private class NewMessagesEventListener extends MessageEventListener {

        public NewMessagesEventListener(CoreServices coreServices) {
            super(coreServices);
        }

        @Override
        protected Query createQuery(Firebase firebase) {
            return firebase.child(chatroom.getFirebasePath())
                    .orderByChild(CREATED_AT).limitToLast(BATCH_SIZE_INITIAL);
        }

        @Override
        protected void set(Query query) {
            query.addValueEventListener(this);
        }

        @Override
        protected void onMessages(TreeSet<Message> messages) {
            if (messages.size() > 0) {
                if (conversationAdapter.getItemCount() > 0) {
                    Intent intent = new Intent(ChatroomsPresenter.ACTION_NEW_MESSAGE);
                    intent.putExtra(ChatroomsPresenter.EXTRA_CHATROOM, new Chatroom(chatroom));
                    intent.putExtra(ChatroomsPresenter.EXTRA_MESSAGE, messages.last());
                    localBroadcastManager.sendBroadcast(intent);
                }

                conversationAdapter.addMessages(messages);
                codementorTasks.markConversationRead(chatroom).continueWith(errorHandler::logAndToastTask, UI);
            }

            if (messages.size() < BATCH_SIZE_INITIAL) {
                getView().setAllMessagesLoaded(true);
            }

            getView().showEmptyState(conversationAdapter.getItemCount() == 0);
        }
    }

    private class OldMessagesEventListener extends MessageEventListener {

        public OldMessagesEventListener(CoreServices coreServices) {
            super(coreServices);
        }

        @Override
        protected Query createQuery(Firebase firebase) {
            return firebase.child(chatroom.getFirebasePath())
                    .orderByChild(CREATED_AT).limitToLast(BATCH_SIZE)
                    .endAt(conversationAdapter.getOldestMessage().getTimestamp() - 1);
        }

        @Override
        protected void set(Query query) {
            query.addListenerForSingleValueEvent(this);
        }

        @Override
        protected void onMessages(TreeSet<Message> messages) {
            conversationAdapter.addMessages(messages);
            if (messages.size() < BATCH_SIZE) {
                getView().setAllMessagesLoaded(true);
            }
        }
    }

    private abstract class MessageEventListener extends QueryEventListener {

        public MessageEventListener(CoreServices coreServices) {
            super(coreServices);
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Task.callInBackground(() -> parse(dataSnapshot))
                    .onSuccess(task -> {
                        onMessages(task.getResult());
                        getView().setRefreshing(false);
                        return null;
                    }, UI)
                    .continueWith(errorHandler::logAndToastTask, UI);
        }

        private TreeSet<Message> parse(DataSnapshot snapshot) {
            TreeSet<Message> messages = new TreeSet<>();

            for (DataSnapshot child : snapshot.getChildren()) {
                MessageData messageData = child.getValue(MessageData.class);
                messages.add(new Message(messageData, userManager.getUsername()));
            }

            return messages;
        }

        protected abstract void onMessages(TreeSet<Message> messages);

    }

}
