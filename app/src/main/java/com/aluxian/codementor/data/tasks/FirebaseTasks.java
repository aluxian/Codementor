package com.aluxian.codementor.data.tasks;

import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.Message;
import com.aluxian.codementor.data.models.MessageData;
import com.aluxian.codementor.data.models.firebase.FirebaseMessage;
import com.aluxian.codementor.utils.ErrorHandler;
import com.aluxian.codementor.utils.UserManager;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import bolts.Task;

public class FirebaseTasks {

    private Firebase firebaseRef;
    private ErrorHandler errorHandler;
    private UserManager userManager;

    public FirebaseTasks(Firebase firebaseRef, ErrorHandler errorHandler, UserManager userManager) {
        this.firebaseRef = firebaseRef;
        this.errorHandler = errorHandler;
        this.userManager = userManager;
    }

    /**
     * @param firebaseToken The Firebase token to authenticate with.
     * @param reAuth        True if this is a re-authentication attempt.
     * @return An {@link AuthData} object.
     */
    public Task<AuthData> authenticate(String firebaseToken, boolean reAuth) {
        Task<AuthData>.TaskCompletionSource taskSource = Task.<AuthData>create();

        firebaseRef.authWithCustomToken(firebaseToken, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                if (reAuth) {
                    userManager.setLoggedIn(userManager.getUsername(), authData.getToken());
                }

                taskSource.setResult(authData);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                taskSource.setError(firebaseError.toException());
            }
        });

        return taskSource.getTask();
    }

    /**
     * @param snapshot The {@link DataSnapshot} to get data from.
     * @return A simple list of {@link Message} objects.
     */
    public Task<List<Message>> parseMessagesSnapshot(DataSnapshot snapshot) {
        return Task.callInBackground(() -> {
            Type listType = new TypeToken<List<MessageData>>() {}.getType();
            Gson gson = new Gson();

            Map<String, Object> data = snapshot.getValue(new GenericTypeIndicator<Map<String, Object>>() {});
            List<MessageData> messageDataList = gson.fromJson(gson.toJsonTree(data.values()), listType);
            List<Message> messages = new ArrayList<>();

            //noinspection Convert2streamapi
            for (MessageData messageData : messageDataList) {
                messages.add(new Message(messageData, errorHandler, userManager.getUsername()));
            }

            Collections.sort(messages, (lhs, rhs) -> {
                if (lhs.getCreatedAt() < rhs.getCreatedAt()) {
                    return 1;
                } else if (lhs.getCreatedAt() > rhs.getCreatedAt()) {
                    return -1;
                } else {
                    return 0;
                }
            });

            return messages;
        });
    }

    /**
     * @param firebaseMessage The {@link FirebaseMessage} to be sent.
     * @param chatroom        The chatroom inside which the message in sent.
     */
    public Task<String> sendMessage(FirebaseMessage firebaseMessage, Chatroom chatroom) {
        Task<String>.TaskCompletionSource taskSource = Task.<String>create();

        firebaseRef.child(chatroom.getFirebasePath()).push().setValue(firebaseMessage, (firebaseError, firebase) -> {
            if (firebaseError != null) {
                taskSource.setError(firebaseError.toException());
            } else {
                taskSource.setResult(firebase.getKey());
            }
        });

        return taskSource.getTask();
    }

}
