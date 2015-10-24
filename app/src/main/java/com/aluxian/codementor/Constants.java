package com.aluxian.codementor;

import android.text.TextUtils;

public class Constants {

    public static final String FIREBASE_URL = "https://codementor.firebaseio.com/";
    public static final String SERVER_API_URL = "https://www.codementor.io/api/";

    public static final String CODEMENTOR_SIGN_IN_URL = "https://www.codementor.io/users/sign_in";
    public static final String CODEMENTOR_FIREBASE_TOKEN_URL = "https://www.codementor.io/terms";

    public static String getApiUrl(String... segments) {
        return SERVER_API_URL + TextUtils.join("/", segments);
    }

}
