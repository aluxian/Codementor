package com.aluxian.codementor.tasks;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginTask extends AsyncTask<String, Void, String> implements Firebase.AuthResultHandler {

    private OkHttpClient okHttpClient;
    private Callbacks callbacks;
    private Exception error;

    private String username;
    private String firebaseToken;

    public LoginTask(OkHttpClient okHttpClient, Callbacks callbacks) {
        this.okHttpClient = okHttpClient;
        this.callbacks = callbacks;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            username = params[0];
            return doSignIn(username, params[1]);
        } catch (IOException e) {
            error = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String firebaseToken) {
        super.onPostExecute(firebaseToken);
        this.firebaseToken = firebaseToken;

        if (firebaseToken != null) {
            callbacks.onFirebaseAuth();
            Firebase ref = new Firebase("https://codementor.firebaseio.com/");
            ref.authWithCustomToken(firebaseToken, this);
        } else {
            callbacks.onAuthError(this.error);
            callbacks.onAuthFinished();
        }
    }

    @Override
    public void onAuthenticated(AuthData authData) {
        callbacks.onAuthFinished();
        callbacks.onAuthSuccessful(username, firebaseToken);
    }

    @Override
    public void onAuthenticationError(FirebaseError firebaseError) {
        callbacks.onAuthFinished();
        callbacks.onAuthError(firebaseError.toException());
    }

    private String doSignIn(String username, String password) throws IOException {
        RequestBody requestBody = new FormEncodingBuilder()
                .add("utf8", "✓")
                .add("authenticity_token", getAuthCode())
                .add("login", username)
                .add("password", password)
                .add("remember_me", "on")
                .build();

        Request request = new Request.Builder()
                .post(requestBody)
                .url("https://www.codementor.io/users/sign_in")
                .build();

        Response response = okHttpClient.newCall(request).execute();
        List<String> headers = response.headers("Location");

        if (headers.size() < 1) {
            throw new IOException("Location header not found");
        }

        boolean hasCorrectStatusCode = response.code() == 302;
        boolean hasCorrectLocation = headers.get(0).equals("https://www.codementor.io/");
        boolean incorrectLogin = headers.get(0).equals("https://www.codementor.io/users/sign_in");

        if (incorrectLogin) {
            throw new IOException("Wrong credentials");
        }

        if (hasCorrectStatusCode && hasCorrectLocation) {
            return getFirebaseToken();
        }

        return null;
    }

    private String getAuthCode() throws IOException {
        String code = null;

        Request request = new Request.Builder().url("https://www.codementor.io/users/sign_in").build();
        Response response = okHttpClient.newCall(request).execute();
        String responseBody = response.body().string();

        Pattern pattern = Pattern.compile("users/sign_in.*?name=\"authenticity_token\" value=\"(.*?)\"");
        Matcher matcher = pattern.matcher(responseBody);

        if (matcher.find()) {
            code = matcher.group(1);
        }

        if (TextUtils.isEmpty(code)) {
            throw new IOException("Couldn't retrieve auth code");
        }

        return code;
    }

    private String getFirebaseToken() throws IOException {
        String token = null;

        // The /terms page is smaller than others (~3k lines)
        Request request = new Request.Builder().url("https://www.codementor.io/terms").build();
        Response response = okHttpClient.newCall(request).execute();
        String responseBody = response.body().string();

        Pattern pattern = Pattern.compile("\"token\":\"(.*?)\"");
        Matcher matcher = pattern.matcher(responseBody);

        if (matcher.find()) {
            token = matcher.group(1);
        }

        if (TextUtils.isEmpty(token)) {
            throw new IOException("Couldn't retrieve Firebase token");
        }

        return token;
    }

    public interface Callbacks {

        /**
         * Called when authentication is successful.
         *
         * @param username      The authenticated user's username.
         * @param firebaseToken The authenticated user's Firebase access token.
         */
        void onAuthSuccessful(String username, String firebaseToken);

        /**
         * Called when authentication is finished, even if it fails.
         */
        void onAuthFinished();

        /**
         * Called when the Firebase authentication process starts.
         */
        void onFirebaseAuth();

        /**
         * Called when an error occurs.
         *
         * @param e The error's Exception.
         */
        void onAuthError(Exception e);

    }

}
