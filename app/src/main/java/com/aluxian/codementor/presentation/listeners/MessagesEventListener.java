package com.aluxian.codementor.presentation.listeners;

import android.support.v4.content.LocalBroadcastManager;

import com.aluxian.codementor.data.models.Message;
import com.aluxian.codementor.services.CoreServices;
import com.aluxian.codementor.services.ErrorHandler;
import com.firebase.client.DataSnapshot;

import java.util.TreeSet;

import bolts.Task;

import static com.aluxian.codementor.utils.Constants.UI;

public abstract class MessagesEventListener extends QueryEventListener {

    protected LocalBroadcastManager localBroadcastManager;
    protected ErrorHandler errorHandler;

    public MessagesEventListener(CoreServices coreServices) {
        super(coreServices);
        localBroadcastManager = coreServices.getLocalBroadcastManager();
        errorHandler = coreServices.getErrorHandler();
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Task.callInBackground(() -> parse(dataSnapshot))
                .onSuccess(task -> {
                    onMessages(task.getResult());
                    return null;
                }, UI)
                .continueWith(errorHandler::logAndToastTask, UI);
    }

    private TreeSet<Message> parse(DataSnapshot dataSnapshot) {
        TreeSet<Message> messages = new TreeSet<>();
        for (DataSnapshot child : dataSnapshot.getChildren()) {
            Message message = child.getValue(Message.class);
            message.setKey(child.getKey());
            messages.add(message);
        }

        return messages;
    }

    protected abstract void onMessages(TreeSet<Message> messages);

}
