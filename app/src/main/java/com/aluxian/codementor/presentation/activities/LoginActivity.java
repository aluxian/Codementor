package com.aluxian.codementor.presentation.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

import com.aluxian.codementor.R;
import com.aluxian.codementor.presentation.presenters.LoginActivityPresenter;
import com.aluxian.codementor.presentation.views.LoginActivityView;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity<LoginActivityPresenter>
        implements LoginActivityView, OnCancelListener {

    @Bind(R.id.input_login) MaterialEditText usernameField;
    @Bind(R.id.input_password) MaterialEditText passwordField;
    @Bind(R.id.btn_login) AppCompatButton loginButton;

    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        getCoreServices().getCookieStore().removeAll();
        setPresenter(new LoginActivityPresenter(this, getCoreServices()));

//        TODO: Make this work
//        passwordField.setOnEditorActionListener((v, actionId, event) -> {
//            getPresenter().logIn();
//            return true;
//        });

        loginButton.setOnClickListener(this::loginButtonClicked);
    }

    @Override
    public void navigateToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void showProgressDialog() {
        progressDialog = new AlertDialog.Builder(this, R.style.AppTheme_Login_Dialog)
                .setOnCancelListener(this)
                .show();
    }

    @Override
    public void updateProgressDialogMessage(int messageResId) {
        progressDialog.setMessage(getString(messageResId));
    }

    @Override
    public void dismissProgressDialog() {
        progressDialog.dismiss();
    }

    @Override
    public void setUsernameFieldError(int stringResId) {
        usernameField.setError(getString(stringResId));
    }

    @Override
    public void setPasswordFieldError(int stringResId) {
        passwordField.setError(getString(stringResId));
    }

    @Override
    public void onCancel(DialogInterface dialog) {
//        dialog.dismiss(); TODO: is required?
        getPresenter().dialogCancelled();
    }

    private void loginButtonClicked(View buttonView) {
        String username = usernameField.getText().toString().trim().toLowerCase();
        String password = passwordField.getText().toString().trim();
        getPresenter().logIn(username, password);
    }

}
