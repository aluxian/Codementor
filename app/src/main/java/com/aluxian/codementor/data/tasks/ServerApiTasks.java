package com.aluxian.codementor.data.tasks;

import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.ChatroomsList;
import com.aluxian.codementor.data.models.ChatroomsListData;
import com.aluxian.codementor.data.models.FirebaseMessage;
import com.aluxian.codementor.data.models.FirebaseServerMessage;
import com.aluxian.codementor.data.utils.CamelCaseNamingStrategy;
import com.aluxian.codementor.services.ErrorHandler;
import com.aluxian.codementor.services.UserManager;
import com.aluxian.codementor.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import bolts.Task;

public class ServerApiTasks {

    private static final MediaType TEXT_PLAIN_MEDIA_TYPE = MediaType.parse("text/plain");
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json;charset=UTF-8");

    private ErrorHandler errorHandler;
    private OkHttpClient okHttpClient;
    private UserManager userManager;

    public ServerApiTasks(OkHttpClient okHttpClient, UserManager userManager, ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        this.okHttpClient = okHttpClient;
        this.userManager = userManager;
    }

    /**
     * @return A {@link ChatroomsList}.
     */
    public Task<ChatroomsList> getChatroomsList() {
        return Task.callInBackground(() -> {
            Request request = new Request.Builder().url(Constants.getChatroomsListUrl()).build();
            Response response = okHttpClient.newCall(request).execute();

            String responseBody = response.body().string();
            ChatroomsListData data = new Gson().fromJson(responseBody, ChatroomsListData.class);

            return new ChatroomsList(data, userManager.getUsername());
        });
    }

    /**
     * @param chatroom The chatroom to be marked as read.
     */
    public Task<Void> markConversationRead(Chatroom chatroom) {
        return Task.callInBackground(() -> {
            Request request = new Request.Builder()
                    .post(RequestBody.create(TEXT_PLAIN_MEDIA_TYPE, ""))
                    .url(chatroom.getOtherUser().getReadPath())
                    .build();

            Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new Exception("Request unsuccessful, returned code " + response.code());
            }

            return null;
        });
    }

    /**
     * @param firebaseMessage A {@link FirebaseMessage} which holds the message data to be sent.
     * @param firebaseKey     The Firebase key of the message.
     */
    public Task<Void> sendMessage(FirebaseMessage firebaseMessage, String firebaseKey) {
        return Task.callInBackground(() -> {
            Gson gson = new GsonBuilder()
                    .setFieldNamingStrategy(new CamelCaseNamingStrategy())
                    .disableHtmlEscaping()
                    .create();

            String requestBody = gson.toJson(new FirebaseServerMessage(firebaseKey, firebaseMessage));
            Request request = new Request.Builder()
                    .post(RequestBody.create(JSON_MEDIA_TYPE, requestBody))
                    .url(firebaseMessage.getReceiver().getChatroomPath())
                    .build();

            Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new Exception("Send message to Codementor server failed with code " + response.code());
            }

            return null;
        });
    }

}
