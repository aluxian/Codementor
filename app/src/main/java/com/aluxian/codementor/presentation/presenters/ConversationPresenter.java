package com.aluxian.codementor.presentation.presenters;

import android.text.TextUtils;

import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.FirebaseMessage;
import com.aluxian.codementor.data.types.MessageType;
import com.aluxian.codementor.presentation.adapters.ConversationAdapter;
import com.aluxian.codementor.presentation.listeners.NewMessagesEventListener;
import com.aluxian.codementor.presentation.listeners.OldMessagesEventListener;
import com.aluxian.codementor.presentation.listeners.PresenceEventListener;
import com.aluxian.codementor.presentation.listeners.QueryEventListener;
import com.aluxian.codementor.presentation.views.ConversationView;
import com.aluxian.codementor.services.CoreServices;
import com.aluxian.codementor.services.ErrorHandler;
import com.aluxian.codementor.tasks.CodementorTasks;
import com.aluxian.codementor.tasks.FirebaseTasks;

import static com.aluxian.codementor.utils.Constants.UI;

public class ConversationPresenter extends Presenter<ConversationView> {

    public static final int BATCH_SIZE_INITIAL = 25;
    public static final int BATCH_SIZE = 100;

    private ErrorHandler errorHandler;
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

        errorHandler = coreServices.getErrorHandler();
        codementorTasks = coreServices.getCodementorTasks();
        firebaseTasks = coreServices.getFirebaseTasks();

        presenceListener = new PresenceEventListener(getView(), chatroom, coreServices);
        messagesListener = new NewMessagesEventListener(getView(), conversationAdapter, chatroom, coreServices);
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
        new OldMessagesEventListener(getView(), conversationAdapter, chatroom, coreServices).start();
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
                null,
                chatroom.getCurrentUser(),
                chatroom.getOtherUser()
        );

        firebaseTasks.sendMessage(firebaseMessage, chatroom)
                .onSuccessTask(task -> codementorTasks.sendMessage(firebaseMessage, task.getResult()))
                .continueWith(errorHandler::logAndToastTask, UI);
    }

}
