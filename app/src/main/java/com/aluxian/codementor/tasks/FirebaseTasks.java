package com.aluxian.codementor.tasks;

import com.aluxian.codementor.data.models.AppData;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.FirebaseMessage;
import com.aluxian.codementor.data.models.Message;
import com.aluxian.codementor.data.models.User;
import com.aluxian.codementor.data.types.PresenceType;
import com.aluxian.codementor.services.UserManager;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.firebase.client.ValueEventListener;

import java.util.Date;
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
     * @param appData {@link AppData} object which contains the token to authenticate with.
     * @param reAuth  True if this is a re-authentication attempt.
     * @return An {@link AuthData} object.
     */
    public Task<AppData> authenticate(AppData appData, boolean reAuth) {
        Task<AppData>.TaskCompletionSource taskSource = Task.<AppData>create();

        firebaseRef.authWithCustomToken(appData.getFirebaseConfig().getToken(), new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                if (reAuth) {
                    userManager.setLoggedIn(appData.getUser().getUsername());
                }

                taskSource.setResult(appData);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                taskSource.setError(firebaseError.toException());
            }
        });

        return taskSource.getTask();
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
     * @param user The user whose presence to retrieve.
     * @return The status of the given user.
     */
    public Task<PresenceType> getPresence(User user) {
        return wrapTaskReAuth(() -> getPresenceImpl(user));
    }

    private Task<PresenceType> getPresenceImpl(User user) {
        Task<PresenceType>.TaskCompletionSource taskSource = Task.<PresenceType>create();

        Firebase presenceRef = firebaseRef.child(user.getPresencePath());
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
     * @param user            The user whose presence to set.
     * @param newPresenceType The new status.
     */
    public Task<Void> setPresence(User user, PresenceType newPresenceType) {
        return wrapTaskReAuth(() -> setPresenceImpl(user, newPresenceType));
    }

    private Task<Void> setPresenceImpl(User user, PresenceType newPresenceType) {
        Task<Void>.TaskCompletionSource taskSource = Task.<Void>create();

        Firebase presenceRef = firebaseRef.child(user.getPresencePath());
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
     * @param chatroom The chatroom of the message.
     * @param message  The message to mark as read.
     */
    public Task<Void> markRead(Chatroom chatroom, Message message) {
        return wrapTaskReAuth(() -> markReadImpl(chatroom, message));
    }

    private Task<Void> markReadImpl(Chatroom chatroom, Message message) {
        Task<Void>.TaskCompletionSource taskSource = Task.<Void>create();

        long value = new Date().getTime();
        Firebase messageRef = firebaseRef.child(chatroom.getFirebasePath())
                .child(message.getKey())
                .child("read_at");

        messageRef.setValue(value, (firebaseError, firebase) -> {
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

            taskSource.setResult(task.getResult());
            return null;
        };
    }

    private <T> void onFirebaseException(Exception error, Task<T>.TaskCompletionSource taskSource,
                                         TaskProvider<T> taskProvider) {
        FirebaseException firebaseException = (FirebaseException) error;
        boolean permissionDenied = firebaseException.getMessage().contains("Permission denied");

        if (permissionDenied) {
            codementorTasks.extractAppData()
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
