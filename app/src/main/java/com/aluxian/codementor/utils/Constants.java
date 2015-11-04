package com.aluxian.codementor.utils;

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

}
