package com.aluxian.codementor.tasks;

import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.FirebaseMessage;
import com.aluxian.codementor.data.models.Message;
import com.aluxian.codementor.data.models.MessageData;
import com.aluxian.codementor.data.types.PresenceType;
import com.aluxian.codementor.services.UserManager;
import com.aluxian.codementor.utils.Constants;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import bolts.Task;

public class FirebaseTasks {

    private Firebase firebaseRef;
    private UserManager userManager;

    public FirebaseTasks(Firebase firebaseRef, UserManager userManager) {
        this.firebaseRef = firebaseRef;
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
            List<Message> messages = new ArrayList<>();

            for (DataSnapshot child : snapshot.getChildren()) {
                MessageData messageData = child.getValue(MessageData.class);
                messages.add(new Message(messageData, userManager.getUsername()));
            }

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

    /**
     * @param username The username of the user whose status to retrieve.
     * @return The status of the given user.
     */
    public Task<PresenceType> getPresence(String username) {
        Task<PresenceType>.TaskCompletionSource taskSource = Task.<PresenceType>create();

        Firebase presenceRef = firebaseRef.child(Constants.presencePath(username));
        presenceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String status = dataSnapshot.getValue(String.class);
                taskSource.setResult(PresenceType.parse(status));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                taskSource.setError(firebaseError.toException());
            }
        });

        return taskSource.getTask();
    }

    /**
     * @param username        The username of the user whose status to set.
     * @param newPresenceType The new status.
     */
    public Task<Void> setPresence(String username, PresenceType newPresenceType) {
        Task<Void>.TaskCompletionSource taskSource = Task.<Void>create();

        Firebase presenceRef = firebaseRef.child(Constants.presencePath(username));
        presenceRef.setValue(newPresenceType.name().toLowerCase(), (firebaseError, firebase) -> {
            if (firebaseError != null) {
                taskSource.setError(firebaseError.toException());
            }
        });

        return taskSource.getTask();
    }

}
