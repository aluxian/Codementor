package com.aluxian.codementor.utils;

import android.text.TextUtils;

public class Constants {

    public static final String FIREBASE_URL = "https://codementor.firebaseio.com/";
    public static final String SERVER_API_URL = "https://www.codementor.io/api/";

    public static final String CODEMENTOR_SIGN_IN_URL = "https://www.codementor.io/users/sign_in";
    public static final String CODEMENTOR_FIREBASE_TOKEN_URL = "https://www.codementor.io/terms";

    public static String getChatroomsListUrl() {
        return getApiUrl("chatrooms", "list");
    }

    public static String getChatroomUrl(String username) {
        return getApiUrl("chatrooms", username);
    }

    public static String getChatroomReadUrl(String username) {
        return getApiUrl("chatrooms", username, "read");
    }

    public static String getRequestUrl(String requestId) {
        return "https://www.codementor.io/questions/" + requestId;
    }

    public static String getPresencePath(String username) {
        return joinPath("presence", username, "magic");
    }

    public static String getChatroomPath(String chatroomFirebaseId, String chatroomId) {
        return joinPath("chatrooms", chatroomFirebaseId, chatroomId);
    }

    private static String getApiUrl(String... segments) {
        return SERVER_API_URL + TextUtils.join("/", segments);
    }

    private static String joinPath(String... segments) {
        return TextUtils.join("/", segments);
    }

}
