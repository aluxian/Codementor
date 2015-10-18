package com.aluxian.codementor.presentation.views;

public interface LoginActivityView extends BaseView {

    void navigateToMainActivity();

    void showProgressDialog();

    void updateProgressDialogMessage(int stringResId);

    void dismissProgressDialog();

    void setUsernameFieldError(int stringResId);

    void setPasswordFieldError(int stringResId);

}
