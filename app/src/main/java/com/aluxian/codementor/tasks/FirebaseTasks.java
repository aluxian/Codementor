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
import com.firebase.client.FirebaseException;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import bolts.Continuation;
import bolts.Task;

public class FirebaseTasks {

    private Firebase firebaseRef;
    private CodementorTasks codementorTasks;
    private UserManager userManager;

    public FirebaseTasks(Firebase firebaseRef, CodementorTasks codementorTasks, UserManager userManager) {
        this.firebaseRef = firebaseRef;
        this.codementorTasks = codementorTasks;
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
        return wrapTaskReAuth(() -> sendMessageImpl(firebaseMessage, chatroom));
    }

    private Task<String> sendMessageImpl(FirebaseMessage firebaseMessage, Chatroom chatroom) {
        Task<String>.TaskCompletionSource taskSource = Task.<String>create();

        firebaseRef.child(chatroom.getFirebasePath()).push()
                .setValue(firebaseMessage, (firebaseError, firebase) -> {
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
        return wrapTaskReAuth(() -> getPresenceImpl(username));
    }

    private Task<PresenceType> getPresenceImpl(String username) {
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
        return wrapTaskReAuth(() -> setPresenceImpl(username, newPresenceType));
    }

    private Task<Void> setPresenceImpl(String username, PresenceType newPresenceType) {
        Task<Void>.TaskCompletionSource taskSource = Task.<Void>create();

        Firebase presenceRef = firebaseRef.child(Constants.presencePath(username));
        String value = newPresenceType.name().toLowerCase();

        presenceRef.setValue(value, (firebaseError, firebase) -> {
            if (firebaseError != null) {
                taskSource.setError(firebaseError.toException());
            } else {
                taskSource.setResult(null);
            }
        });

        return taskSource.getTask();
    }

    /**
     * Wrap the given task so that Firebase "Permission denied" errors (caused by an invalid Firebase token) are
     * handled. In case of such an error, Firebase authentication is retried, then the task will be ran again.
     *
     * @param taskProvider A provider for the task to run.
     * @param <T>          The result type of the task.
     * @return A new task.
     */
    public <T> Task<T> wrapTaskReAuth(TaskProvider<T> taskProvider) {
        Task<T>.TaskCompletionSource taskSource = Task.<T>create();
        taskProvider.get().continueWith(this.<T>onWrappedTaskCompleted(taskSource, taskProvider));
        return taskSource.getTask();
    }

    private <T> Continuation<T, T> onWrappedTaskCompleted(Task<T>.TaskCompletionSource taskSource,
                                                          TaskProvider<T> taskProvider) {
        return task -> {
            Exception error = task.getError();

            if (task.isCancelled()) {
                if (error != null) {
                    throw new CancellationException(error.getMessage());
                } else {
                    throw new CancellationException();
                }
            }

            if (task.isFaulted()) {
                if (error instanceof FirebaseException) {
                    this.<T>onFirebaseException(error, taskSource, taskProvider);
                } else {
                    taskSource.setError(error);
                }

                return null;
            }

            return task.getResult();
        };
    }

    private <T> void onFirebaseException(Exception error, Task<T>.TaskCompletionSource taskSource,
                                         TaskProvider<T> taskProvider) {
        FirebaseException firebaseException = (FirebaseException) error;
        boolean permissionDenied = firebaseException.getMessage().contains("Permission denied");

        if (permissionDenied) {
            codementorTasks.extractToken()
                    .onSuccessTask(task -> authenticate(task.getResult(), true))
                    .continueWith(task -> {
                        if (task.isCancelled() || task.isFaulted()) {
                            taskSource.setError(error);
                        } else {
                            taskProvider.get().continueWith(this.<T>onWrappedTaskReCompleted(taskSource));
                        }

                        return null;
                    });
        } else {
            taskSource.setError(error);
        }
    }

    private <T> Continuation<T, T> onWrappedTaskReCompleted(Task<T>.TaskCompletionSource taskSource) {
        return task -> {
            Exception error = task.getError();

            if (task.isCancelled()) {
                if (error != null) {
                    throw new CancellationException(error.getMessage());
                } else {
                    throw new CancellationException();
                }
            } else if (task.isFaulted()) {
                taskSource.setError(error);
            } else {
                taskSource.setResult(task.getResult());
            }

            return null;
        };
    }

    public interface TaskProvider<T> {

        /**
         * @return A new task.
         */
        Task<T> get();

    }

}
