package com.aluxian.codementor.utils;

import android.text.TextUtils;

import java.util.concurrent.Executor;

import bolts.Task;

public class Constants {

    public static final Executor UI = Task.UI_THREAD_EXECUTOR;
    public static final Executor BG = Task.BACKGROUND_EXECUTOR;

    public static final String FIREBASE_URL = "https://codementor.firebaseio.com/";
    public static final String SERVER_API_URL = "https://www.codementor.io/api/";

    public static final String CODEMENTOR_SIGN_IN_URL = "https://www.codementor.io/users/sign_in";
    public static final String CODEMENTOR_FIREBASE_TOKEN_URL = "https://www.codementor.io/terms";
    public static final String CODEMENTOR_QUESTIONS_URL = "https://www.codementor.io/questions";

    public static String chatroomsListUrl() {
        return apiUrl("chatrooms", "list");
    }

    public static String chatroomUrl(String username) {
        return apiUrl("chatrooms", username);
    }

    public static String chatroomReadUrl(String username) {
        return apiUrl("chatrooms", username, "read");
    }

    public static String requestUrl(String requestId) {
        return join(CODEMENTOR_QUESTIONS_URL, requestId);
    }

    public static String presencePath(String username) {
        return join("presence", username, "magic");
    }

    public static String chatroomPath(String chatroomFirebaseId, String chatroomId) {
        return join("chatrooms", chatroomFirebaseId, chatroomId);
    }

    private static String apiUrl(String... segments) {
        return SERVER_API_URL + join(segments);
    }

    private static String join(String... segments) {
        return TextUtils.join("/", segments);
    }

}
