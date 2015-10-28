package com.aluxian.codementor.presentation.presenters;

import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;
import android.text.TextUtils;

import com.aluxian.codementor.data.events.NewMessageEvent;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.ConversationItem;
import com.aluxian.codementor.data.models.FirebaseMessage;
import com.aluxian.codementor.data.models.Message;
import com.aluxian.codementor.data.types.MessageType;
import com.aluxian.codementor.data.types.PresenceType;
import com.aluxian.codementor.presentation.adapters.ConversationAdapter;
import com.aluxian.codementor.presentation.views.ConversationView;
import com.aluxian.codementor.services.CoreServices;
import com.aluxian.codementor.services.ErrorHandler;
import com.aluxian.codementor.services.UserManager;
import com.aluxian.codementor.tasks.CodementorTasks;
import com.aluxian.codementor.tasks.FirebaseTasks;
import com.aluxian.codementor.tasks.ServerApiTasks;
import com.aluxian.codementor.utils.Constants;
import com.aluxian.codementor.utils.SortedListCallback;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.squareup.otto.Bus;

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
    private ServerApiTasks serverApiTasks;

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
        serverApiTasks = coreServices.getServerApiTasks();

        Firebase firebaseRef = coreServices.getFirebaseRef();
        presenceRef = firebaseRef.child(Constants.presencePath(chatroom.getOtherUser().getUsername()));
        messagesRef = firebaseRef.child(chatroom.getFirebasePath()).orderByChild(CREATED_AT).limitToLast(BATCH_SIZE);

        initAdapter();
    }

    private void initAdapter() {
        ConversationAdapter adapter = new ConversationAdapter();
        getView().setAdapter(adapter);
        items = new SortedList<>(ConversationItem.class, new SortedListCallback<>(adapter));
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
                .onSuccessTask(task -> serverApiTasks.sendMessage(firebaseMessage, task.getResult()))
                .continueWith(errorHandler::logAndToastTask, UI);
    }

    private void updateStatus(@Nullable String status) {
        if (status == null) {
            getView().setSubtitle(null);
            return;
        }

        getView().setSubtitle(PresenceType.parse(status).statusResId);
    }

    private void handleFirebaseError(FirebaseError firebaseError) {
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
                .onSuccess(this::onFirebaseReAuthSuccessful, UI)
                .continueWith(this::onFirebaseReAuthCompleted, UI);
    }

    private Void onFirebaseReAuthSuccessful(Task task) {
        removeFirebaseListeners();
        addFirebaseListeners();
        return null;
    }

    private Void onFirebaseReAuthCompleted(Task task) {
        if (task.isFaulted()) {
            errorHandler.logAndToast(task.getError());
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private List<ConversationItem> castList(List<? extends ConversationItem> items) {
        return (List<ConversationItem>) items;
    }

    private Void onMessagesLoaded(Task<List<Message>> task) {
        List<Message> messages = task.getResult();

        if (messages.size() > 0) {
            if (items.size() > 0) {
                bus.post(new NewMessageEvent(chatroom, messages.get(messages.size() - 1)));
            }

            serverApiTasks.markConversationRead(chatroom)
                    .continueWith(errorHandler::logAndToastTask, UI);
        }

        if (messages.size() < BATCH_SIZE) {
            getView().setAllMessagesLoaded(true);
        }

        items.addAll(castList(messages));
        getView().showEmptyState(items.size() == 0);

        return null;
    }

    private Void onOldMessagesLoaded(Task<List<Message>> task) {
        List<Message> messages = task.getResult();
        items.addAll(castList(messages));

        if (messages.size() < BATCH_SIZE) {
            getView().setAllMessagesLoaded(true);
        }

        return null;
    }

    private Void onLoadingFinished(Task task) {
        getView().setRefreshing(false);
        return null;
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

    private ManagedValueEventListener messagesEventListener = new ManagedValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            firebaseTasks.parseMessagesSnapshot(dataSnapshot)
                    .onSuccess(ConversationPresenter.this::onMessagesLoaded, UI)
                    .continueWith(errorHandler::logAndToastTask, UI)
                    .continueWith(ConversationPresenter.this::onLoadingFinished, UI);
        }

    };

    private ManagedValueEventListener oldMessagesEventListener = new ManagedValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            firebaseTasks.parseMessagesSnapshot(dataSnapshot)
                    .onSuccess(ConversationPresenter.this::onOldMessagesLoaded, UI)
                    .continueWith(errorHandler::logAndToastTask, UI)
                    .continueWith(ConversationPresenter.this::onLoadingFinished, UI);
        }

    };

    private abstract class ManagedValueEventListener implements ValueEventListener {

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            handleFirebaseError(firebaseError);
        }

    }

}
