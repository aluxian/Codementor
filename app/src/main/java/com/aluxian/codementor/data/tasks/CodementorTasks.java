package com.aluxian.codementor.data.tasks;

import android.text.TextUtils;

import com.aluxian.codementor.utils.Constants;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bolts.Task;

public class CodementorTasks {

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

}
