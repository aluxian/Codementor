package com.aluxian.codementor.ui.activities;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.aluxian.codementor.R;
import com.aluxian.codementor.presentation.presenters.LoginActivityPresenter;
import com.aluxian.codementor.presentation.views.LoginActivityView;
import com.aluxian.codementor.ui.fragments.LoadingDialogFragment;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity<LoginActivityPresenter>
        implements LoginActivityView, OnCancelListener {

    private static final String LOADING_DIALOG_TAG = "loading_dialog";

    @Bind(R.id.input_login) MaterialEditText usernameField;
    @Bind(R.id.input_password) MaterialEditText passwordField;
    @Bind(R.id.btn_login) AppCompatButton loginButton;

    private LoadingDialogFragment loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
        setPresenter(new LoginActivityPresenter(this, getCoreServices()));

        loginButton.setOnClickListener(this::loginButtonClicked);
        passwordField.setOnEditorActionListener(this::editorActionListener);
    }

    @Override
    public void navigateToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void showProgressDialog(int messageResId) {
        loadingDialog = LoadingDialogFragment.newInstance(messageResId);
        loadingDialog.show(getSupportFragmentManager(), LOADING_DIALOG_TAG);
        loadingDialog.setOnCancelListener(this);
    }

    @Override
    public void updateProgressDialogMessage(int messageResId) {
        loadingDialog.setMessage(messageResId);
    }

    @Override
    public void dismissProgressDialog() {
        loadingDialog.dismiss();
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
        getPresenter().dialogCancelled();
    }

    private void loginButtonClicked(View buttonView) {
        String username = usernameField.getText().toString().trim().toLowerCase();
        String password = passwordField.getText().toString();
        getPresenter().logIn(username, password);
    }

    private boolean editorActionListener(TextView textView, int actionId, KeyEvent event) {
        loginButtonClicked(null);
        return false;
    }

}
