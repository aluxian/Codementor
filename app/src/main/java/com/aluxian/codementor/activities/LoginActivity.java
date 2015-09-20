package com.aluxian.codementor.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.aluxian.codementor.App;
import com.aluxian.codementor.R;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements DialogInterface.OnCancelListener {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private MaterialEditText loginField;
    private MaterialEditText passwordField;
    private AlertDialog progressDialog;
    private LoginTask loginTask;
    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        app = (App) getApplication();
        app.getCookieStore().removeAll();

        loginField = (MaterialEditText) findViewById(R.id.input_login);
        passwordField = (MaterialEditText) findViewById(R.id.input_password);
        passwordField.setOnEditorActionListener((v, actionId, event) -> {
            logIn();
            return true;
        });

        AppCompatButton loginButton = (AppCompatButton) findViewById(R.id.btn_login);
        loginButton.setOnClickListener(v -> logIn());
    }

    private void logIn() {
        String login = loginField.getText().toString().trim().toLowerCase();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(login)) {
            loginField.setError(getString(R.string.error_field_required));
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordField.setError(getString(R.string.error_field_required));
            return;
        }

        progressDialog = new AlertDialog.Builder(this, R.style.AppTheme_Login_Dialog)
                .setMessage(R.string.msg_authenticating)
                .setOnCancelListener(this)
                .show();

        loginTask = new LoginTask(app.getOkHttpClient(), login);
        loginTask.execute(login, password);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        loginTask.cancel(true);
        loginTask = null;
    }

    private class LoginTask extends AsyncTask<String, Void, String> {

        private OkHttpClient okHttpClient;
        private String errorMessage;
        private String login;

        private LoginTask(OkHttpClient okHttpClient, String login) {
            this.okHttpClient = okHttpClient;
            this.login = login;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return doSignIn(params[0], params[1]);
            } catch (IOException e) {
                errorMessage = e.getMessage();
                Log.e(TAG, e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String firebaseToken) {
            super.onPostExecute(firebaseToken);

            if (firebaseToken != null) {
                progressDialog.setMessage(getString(R.string.msg_firebase_auth));

                Firebase ref = new Firebase("https://codementor.firebaseio.com/");
                ref.authWithCustomToken(firebaseToken, new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        progressDialog.dismiss();
                        app.getUserManager().setLoggedIn(login, firebaseToken);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        progressDialog.dismiss();
                        Log.e(TAG, firebaseError.getMessage(), firebaseError.toException());
                        handleError(firebaseError.getMessage());
                    }
                });
            } else {
                handleError(this.errorMessage);
                progressDialog.dismiss();
            }
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
                Log.e(TAG, "Location header not found");
                return null;
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

        private void handleError(String errorMessage) {
            if (TextUtils.isEmpty(errorMessage)) {
                errorMessage = getString(R.string.error_unexpected);
            }

            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
        }

    }

}
