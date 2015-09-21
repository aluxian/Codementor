package com.aluxian.codementor.tasks;

import android.os.AsyncTask;

import com.aluxian.codementor.models.Chatroom;
import com.aluxian.codementor.models.Message;
import com.aluxian.codementor.models.User;
import com.aluxian.codementor.models.firebase.FirebaseMessage;
import com.aluxian.codementor.models.firebase.FirebaseServerMessage;
import com.aluxian.codementor.utils.CamelCaseNamingStrategy;
import com.aluxian.codementor.utils.UserManager;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class SendMessageTask extends AsyncTask<String, Void, Void> implements Firebase.CompletionListener {

    private OkHttpClient okHttpClient;
    private UserManager userManager;
    private Callbacks callbacks;
    private Chatroom chatroom;
    private FirebaseMessage firebaseMessage;

    public SendMessageTask(OkHttpClient okHttpClient, UserManager userManager, Callbacks callbacks, Chatroom chatroom) {
        this.okHttpClient = okHttpClient;
        this.chatroom = chatroom;
        this.userManager = userManager;
        this.callbacks = callbacks;
    }

    @Override
    protected Void doInBackground(String... params) {
        User receiver = chatroom.getOtherUser(userManager.getUsername());
        User sender = chatroom.getOtherUser(receiver.getUsername());

        firebaseMessage = new FirebaseMessage(chatroom.getChatroomId(),
                Message.Type.MESSAGE, params[0], sender, receiver);

        Firebase ref = new Firebase("https://codementor.firebaseio.com/");
        ref.child(chatroom.getFirebasePath()).push().setValue(firebaseMessage, this);

        return null;
    }

    @Override
    public void onComplete(FirebaseError firebaseError, Firebase ref) {
        if (firebaseError != null) {
            callbacks.onBackgroundError(firebaseError.toException());
            return;
        }

        new Thread(() -> {
            try {
                saveOnServer(firebaseMessage, ref.getKey());
            } catch (IOException e) {
                callbacks.onBackgroundError(e);
            }
        }).start();
    }

    private void saveOnServer(FirebaseMessage firebaseMessage, String firebaseKey) throws IOException {
        FirebaseServerMessage firebaseServerMessage = new FirebaseServerMessage(firebaseKey, firebaseMessage);
        String requestBody = new GsonBuilder()
                .setFieldNamingStrategy(new CamelCaseNamingStrategy())
                .create()
                .toJson(firebaseServerMessage);

        Request request = new Request.Builder()
                .post(RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), requestBody))
                .url("https://www.codementor.io/api/chatrooms/" + userManager.getUsername())
                .build();

        Response response = okHttpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("POST message to Codementor server failed with code " + response.code());
        }
    }

    public interface Callbacks {

        /**
         * Called when an error occurs.
         *
         * @param e The error's Exception.
         */
        void onBackgroundError(Exception e);

    }

}
