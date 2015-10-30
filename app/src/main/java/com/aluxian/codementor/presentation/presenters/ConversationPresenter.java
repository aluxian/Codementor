package com.aluxian.codementor.presentation.presenters;

import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;
import android.text.TextUtils;

import com.aluxian.codementor.data.events.NewMessageEvent;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.ConversationItem;
import com.aluxian.codementor.data.models.FirebaseMessage;
import com.aluxian.codementor.data.models.Message;
import com.aluxian.codementor.data.models.MessageData;
import com.aluxian.codementor.data.models.TimeMarker;
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
import com.aluxian.codementor.utils.Helpers;
import com.aluxian.codementor.utils.SortedListCallback;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bolts.Task;

import static com.aluxian.codementor.utils.Constants.UI;

public class ConversationPresenter extends Presenter<ConversationView> {

    private static final String CREATED_AT = "created_at";
    private static final int BATCH_SIZE = 100;

    private Bus bus;
    private ErrorHandler errorHandler;
    private UserManager userManager;

    private CodementorTasks codementorTasks;
    private FirebaseTasks firebaseTasks;

    private Firebase presenceRef;
    private Query messagesRef;

    private @Nullable Task firebaseReAuthTask;
    private SortedList<ConversationItem> items;
    private Chatroom chatroom;

    public ConversationPresenter(ConversationView baseView, CoreServices coreServices, Chatroom chatroom) {
        super(baseView);
        this.chatroom = chatroom;

        bus = coreServices.getBus();
        errorHandler = coreServices.getErrorHandler();
        userManager = coreServices.getUserManager();

        codementorTasks = coreServices.getCodementorTasks();
        firebaseTasks = coreServices.getFirebaseTasks();

        Firebase firebaseRef = coreServices.getFirebaseRef();
        presenceRef = firebaseRef.child(Constants.presencePath(chatroom.getOtherUser().getUsername()));
        messagesRef = firebaseRef.child(chatroom.getFirebasePath()).orderByChild(CREATED_AT).limitToLast(BATCH_SIZE);

        initAdapter();
    }

    private void initAdapter() {
        ConversationAdapter adapter = new ConversationAdapter();
        getView().setAdapter(adapter);
        items = new SortedList<>(ConversationItem.class, new SortedListCallback<>(adapter, true));
        adapter.setHasStableIds(true);
        adapter.setList(items);
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

    public void loadMore() {
        if (items.size() == 0) {
            return;
        }

        getView().setRefreshing(true);
        messagesRef.endAt(items.get(0).getTimestamp() - 1)
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

        items.add(new Message(firebaseMessage, userManager.getUsername()));
        firebaseTasks.sendMessage(firebaseMessage, chatroom)
                .onSuccessTask(task -> codementorTasks.sendMessage(firebaseMessage, task.getResult()))
                .continueWith(errorHandler::logAndToastTask, UI);
    }

    private void updateStatus(@Nullable String status) {
        if (status == null) {
            getView().setSubtitle(null);
            return;
        }

        getView().setSubtitle(PresenceType.parse(status).statusResId);
    }

    private void addItemsToList(List<? extends ConversationItem> itemsList) {
        //noinspection unchecked
        List<ConversationItem> itemsToAdd = (List<ConversationItem>) itemsList;
        List<ConversationItem> newItems = new ArrayList<>();
        ConversationItem item1 = null;

        for (ConversationItem item2 : itemsToAdd) {
            if (item1 == null) {
                long firstTimestamp = item2.getTimestamp();
                Date firstDate = new Date(firstTimestamp);

                if (!Helpers.isSameDay(firstDate, new Date())) {
                    newItems.add(new TimeMarker(firstTimestamp - 1));
                }

                item1 = item2;
                newItems.add(item2);

                continue;
            }

            long timestamp1 = item1.getTimestamp();
            long timestamp2 = item2.getTimestamp();

            if (!Helpers.isSameDay(timestamp1, timestamp2)) {
                newItems.add(new TimeMarker(timestamp2 - 1));
            }

            newItems.add(item2);
            item1 = item2;
        }

        items.addAll(newItems);
    }

    private List<Message> parseMessagesSnapshot(DataSnapshot snapshot) {
        List<Message> messages = new ArrayList<>();

        for (DataSnapshot child : snapshot.getChildren()) {
            MessageData messageData = child.getValue(MessageData.class);
            messages.add(new Message(messageData, userManager.getUsername()));
        }

        return messages;
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

    };

    private ValueEventListener messagesEventListener = new ManagedValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Task.callInBackground(() -> parseMessagesSnapshot(dataSnapshot))
                    .continueWith(task -> {
                        List<Message> messages = task.getResult();

                        if (messages.size() > 0) {
                            if (items.size() > 0) {
                                bus.post(new NewMessageEvent(chatroom, messages.get(messages.size() - 1)));
                            }

                            codementorTasks.markConversationRead(chatroom)
                                    .continueWith(errorHandler::logAndToastTask, UI);
                        }

                        if (messages.size() < BATCH_SIZE) {
                            getView().setAllMessagesLoaded(true);
                        }

                        addItemsToList(messages);
                        getView().showEmptyState(items.size() == 0);
                        getView().setRefreshing(false);

                        return null;
                    }, UI);
        }

    };

    private ValueEventListener oldMessagesEventListener = new ManagedValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Task.callInBackground(() -> parseMessagesSnapshot(dataSnapshot))
                    .continueWith(task -> {
                        List<Message> messages = task.getResult();
                        addItemsToList(messages);

                        if (messages.size() < BATCH_SIZE) {
                            getView().setAllMessagesLoaded(true);
                        }

                        getView().setRefreshing(false);
                        return null;
                    }, UI);
        }

    };

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
