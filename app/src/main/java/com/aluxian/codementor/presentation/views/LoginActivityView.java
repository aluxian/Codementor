package com.aluxian.codementor.presentation.views;

public interface LoginActivityView extends BaseView {

    /**
     * Finish the login activity and go back to main.
     */
    void navigateToMainActivity();

    /**
     * Display a progress dialog.
     *
     * @param messageResId The resource ID of the message string.
     */
    void showProgressDialog(int messageResId);

    /**
     * Update the message of the progress dialog.
     *
     * @param stringResId The resource id of the new string.
     */
    void updateProgressDialogMessage(int stringResId);

    /**
     * Dismiss the progress dialog.
     */
    void dismissProgressDialog();

    /**
     * Updates the error message of the username field.
     *
     * @param stringResId The resource id of the new string.
     */
    void setUsernameFieldError(int stringResId);

    /**
     * Updates the error message of the password field.
     *
     * @param stringResId The resource id of the new string.
     */
    void setPasswordFieldError(int stringResId);

}
