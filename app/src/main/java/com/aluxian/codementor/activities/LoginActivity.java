package com.aluxian.codementor.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.aluxian.codementor.App;
import com.aluxian.codementor.R;
import com.aluxian.codementor.tasks.LoginTask;
import com.rengwuxian.materialedittext.MaterialEditText;

public class LoginActivity extends AppCompatActivity
        implements DialogInterface.OnCancelListener, LoginTask.Callbacks {

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
        String username = loginField.getText().toString().trim().toLowerCase();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
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

        loginTask = new LoginTask(app.getOkHttpClient(), this);
        loginTask.execute(username, password);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        loginTask.cancel(true);
        loginTask = null;
    }

    @Override
    public void onAuthSuccessful(String username, String firebaseToken) {
        app.getUserManager().setLoggedIn(username, firebaseToken);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onAuthFinished() {
        progressDialog.dismiss();
    }

    @Override
    public void onFirebaseAuth() {
        progressDialog.setMessage(getString(R.string.msg_firebase_auth));
    }

    @Override
    public void onAuthError(Exception e) {
        Log.e(TAG, e.getMessage(), e);
        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

}
