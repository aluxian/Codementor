package com.aluxian.codementor.tasks;

import android.text.TextUtils;

import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.ChatroomsList;
import com.aluxian.codementor.data.models.ChatroomsListData;
import com.aluxian.codementor.data.models.FirebaseMessage;
import com.aluxian.codementor.data.models.FirebaseServerMessage;
import com.aluxian.codementor.services.UserManager;
import com.aluxian.codementor.utils.CamelCaseNamingStrategy;
import com.aluxian.codementor.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bolts.Task;

public class CodementorTasks {

    private static final MediaType TEXT_PLAIN_MEDIA_TYPE = MediaType.parse("text/plain");
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json;charset=UTF-8");

    private static final Pattern FIREBASE_TOKEN_PATTERN = Pattern.compile("\"token\":\"(.*?)\"");
    private static final Pattern AUTH_CODE_PATTERN =
            Pattern.compile("users/sign_in.*?name=\"authenticity_token\" value=\"(.*?)\"");

    private OkHttpClient okHttpClient;
    private UserManager userManager;

    public CodementorTasks(OkHttpClient okHttpClient, UserManager userManager) {
        this.okHttpClient = okHttpClient;
        this.userManager = userManager;
    }

    /**
     * @return An auth code extracted from Codementor's sign in page.
     */
    public Task<String> extractAuthCode() {
        return Task.callInBackground(() -> {
            String code = null;

            Request request = new Request.Builder().url(Constants.CODEMENTOR_SIGN_IN_URL).build();
            Response response = okHttpClient.newCall(request).execute();

            String responseBody = response.body().string();
            Matcher matcher = AUTH_CODE_PATTERN.matcher(responseBody);

            if (matcher.find()) {
                code = matcher.group(1);
            }

            if (TextUtils.isEmpty(code)) {
                throw new Exception("Couldn't retrieve auth code");
            }

            return code;
        });
    }

    /**
     * @return A Firebase token extracted from Codementor's website.
     */
    public Task<String> extractToken() {
        return Task.callInBackground(() -> {
            String token = null;

            Request request = new Request.Builder().url(Constants.CODEMENTOR_FIREBASE_TOKEN_URL).build();
            Response response = okHttpClient.newCall(request).execute();

            String responseBody = response.body().string();
            Matcher matcher = FIREBASE_TOKEN_PATTERN.matcher(responseBody);

            if (matcher.find()) {
                token = matcher.group(1);
            }

            if (TextUtils.isEmpty(token)) {
                throw new Exception("Couldn't retrieve Firebase token");
            }

            return token;
        });
    }

    /**
     * @param username The user's username.
     * @param password The user's password.
     * @param authCode Authenticity code required for the sign in process.
     */
    public Task<Void> signIn(String username, String password, String authCode) {
        return Task.callInBackground(() -> {
            RequestBody requestBody = new FormEncodingBuilder()
                    .add("utf8", "✓")
                    .add("authenticity_token", authCode)
                    .add("login", username)
                    .add("password", password)
                    .add("remember_me", "on")
                    .build();

            Request request = new Request.Builder()
                    .post(requestBody)
                    .url(Constants.CODEMENTOR_SIGN_IN_URL)
                    .build();

            Response response = okHttpClient.newCall(request).execute();
            response.body().close();

            List<String> headers = response.headers("Location");

            if (response.code() != 302) {
                throw new Exception("Request unsuccessful, returned code " + response.code());
            }

            if (headers.size() < 1) {
                throw new Exception("Location header not found");
            }

            // Redirected to the same page
            if (headers.get(0).equals(Constants.CODEMENTOR_SIGN_IN_URL)) {
                throw new Exception("Wrong credentials");
            }

            return null;
        });
    }

    /**
     * @return A {@link ChatroomsList}.
     */
    public Task<ChatroomsList> getChatroomsList() {
        return Task.callInBackground(() -> {
            Request request = new Request.Builder().url(Constants.chatroomsListUrl()).build();
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
            response.body().close();

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
            response.body().close();

            if (!response.isSuccessful()) {
                throw new Exception("Send message to Codementor server failed with code " + response.code());
            }

            return null;
        });
    }

}
