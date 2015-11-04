package com.aluxian.codementor.tasks;

import android.text.TextUtils;

import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.ChatroomsList;
import com.aluxian.codementor.data.models.FirebaseMessage;
import com.aluxian.codementor.data.models.FirebaseServerMessage;
import com.aluxian.codementor.utils.Constants;
import com.bluelinelabs.logansquare.LoganSquare;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
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

    public CodementorTasks(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    /**
     * @return An auth code extracted from Codementor's sign in page.
     */
    public Task<String> extractAuthCode() {
        Task<String>.TaskCompletionSource taskSource = Task.<String>create();

        Request request = new Request.Builder()
                .url(Constants.CODEMENTOR_SIGN_IN_URL)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                taskSource.setError(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String body = response.body().string();
                String code = null;

                if (!response.isSuccessful()) {
                    throw new IOException("Couldn't retrieve auth code");
                }

                Matcher matcher = AUTH_CODE_PATTERN.matcher(body);
                if (matcher.find()) {
                    code = matcher.group(1);
                }

                if (TextUtils.isEmpty(code)) {
                    throw new IOException("The extracted auth code was empty");
                }

                taskSource.setResult(code);
            }
        });

        return taskSource.getTask();
    }

    /**
     * @return A Firebase token extracted from Codementor's website.
     */
    public Task<String> extractToken() {
        Task<String>.TaskCompletionSource taskSource = Task.<String>create();

        Request request = new Request.Builder()
                .url(Constants.CODEMENTOR_FIREBASE_TOKEN_URL)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                taskSource.setError(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String body = response.body().string();
                String token = null;

                if (!response.isSuccessful()) {
                    throw new IOException("Couldn't retrieve Firebase token");
                }

                Matcher matcher = FIREBASE_TOKEN_PATTERN.matcher(body);
                if (matcher.find()) {
                    token = matcher.group(1);
                }

                if (TextUtils.isEmpty(token)) {
                    throw new IOException("Extracted Firebase token was empty");
                }

                taskSource.setResult(token);
            }
        });

        return taskSource.getTask();
    }

    /**
     * @param username The user's username.
     * @param password The user's password.
     * @param authCode Authenticity code required for the sign in process.
     */
    public Task<Void> signIn(String username, String password, String authCode) {
        Task<Void>.TaskCompletionSource taskSource = Task.<Void>create();

        boolean initialFollowRedirects = okHttpClient.getFollowRedirects();
        okHttpClient.setFollowRedirects(false);

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

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                taskSource.setError(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                response.body().close();

                if (response.code() != 302) {
                    throw new IOException("Couldn't sign in, server returned code " + response.code());
                }

                List<String> headers = response.headers("Location");
                if (headers.size() < 1) {
                    throw new IOException("Location header not found");
                }

                // Redirected to the same page
                if (headers.get(0).equals(Constants.CODEMENTOR_SIGN_IN_URL)) {
                    throw new IOException("Wrong credentials");
                }

                taskSource.setResult(null);
            }
        });

        okHttpClient.setFollowRedirects(initialFollowRedirects);
        return taskSource.getTask();
    }

    /**
     * @return A {@link ChatroomsList}.
     */
    public Task<ChatroomsList> getChatroomsList() {
        Task<ChatroomsList>.TaskCompletionSource taskSource = Task.<ChatroomsList>create();

        Request request = new Request.Builder()
                .url(Constants.SERVER_API_URL + "chatrooms/list")
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                taskSource.setError(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Couldn't retrieve chatrooms list");
                }

                InputStream body = response.body().byteStream();
                ChatroomsList list = LoganSquare.parse(body, ChatroomsList.class);
                response.body().close();

                taskSource.setResult(list);
            }
        });

        return taskSource.getTask();
    }

    /**
     * @param chatroom The chatroom to be marked as read.
     */
    public Task<Void> markConversationRead(Chatroom chatroom) {
        Task<Void>.TaskCompletionSource taskSource = Task.<Void>create();

        Request request = new Request.Builder()
                .post(RequestBody.create(TEXT_PLAIN_MEDIA_TYPE, ""))
                .url(chatroom.getOtherUser().getReadPath())
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                taskSource.setError(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                response.body().close();

                if (!response.isSuccessful()) {
                    throw new IOException("Couldn't mark message as read, code " + response.code());
                }

                taskSource.setResult(null);
            }
        });

        return taskSource.getTask();
    }

    /**
     * @param firebaseMessage A {@link FirebaseMessage} which holds the message data to be sent.
     * @param firebaseKey     The Firebase key of the message.
     */
    public Task<Void> sendMessage(FirebaseMessage firebaseMessage, String firebaseKey) {
        FirebaseServerMessage serverMessage = new FirebaseServerMessage(firebaseKey, firebaseMessage);
        String requestBody;

        try {
            requestBody = LoganSquare.serialize(serverMessage);
        } catch (IOException e) {
            return Task.forError(e);
        }

        Task<Void>.TaskCompletionSource taskSource = Task.<Void>create();

        Request request = new Request.Builder()
                .post(RequestBody.create(JSON_MEDIA_TYPE, requestBody))
                .url(firebaseMessage.getReceiver().getChatroomPath())
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                taskSource.setError(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                response.body().close();

                if (!response.isSuccessful()) {
                    throw new IOException("Send message to Codementor server failed with code " + response.code());
                }

                taskSource.setResult(null);
            }
        });

        return taskSource.getTask();
    }

}
