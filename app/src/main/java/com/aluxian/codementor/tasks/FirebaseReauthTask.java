package com.aluxian.codementor.tasks;

import android.os.AsyncTask;

import com.aluxian.codementor.utils.UserManager;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;

public class FirebaseReAuthTask extends AsyncTask<Void, Void, String> {

    private Firebase firebaseRef;
    private OkHttpClient okHttpClient;
    private UserManager userManager;
    private FirebaseError firebaseError;
    private Callbacks callbacks;
    private Exception error;

    public FirebaseReAuthTask(Firebase firebaseRef, OkHttpClient okHttpClient, UserManager userManager,
                              FirebaseError firebaseError, Callbacks callbacks) {
        this.firebaseRef = firebaseRef;
        this.okHttpClient = okHttpClient;
        this.userManager = userManager;
        this.firebaseError = firebaseError;
        this.callbacks = callbacks;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            return LoginTask.getFirebaseToken(okHttpClient);
        } catch (IOException e) {
            error = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String firebaseToken) {
        super.onPostExecute(firebaseToken);

        if (error != null) {
            callbacks.onFirebaseReAuthError(firebaseError.toException());
            return;
        }

        if (firebaseToken != null) {
            firebaseRef.authWithCustomToken(firebaseToken, new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    userManager.setLoggedIn(userManager.getUsername(), firebaseToken);
                    callbacks.restartRefresh();
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    callbacks.onFirebaseReAuthError(firebaseError.toException());
                }
            });

            return;
        }

        callbacks.restartRefresh();
    }

    public interface Callbacks {

        void restartRefresh();

        void onFirebaseReAuthError(Exception e);

    }

}
