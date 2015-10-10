package com.aluxian.codementor.tasks;

import android.os.AsyncTask;

import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;

public class SetMessagesReadTask extends AsyncTask<Void, Void, Void> {

    private String contactUsername;
    private OkHttpClient okHttpClient;
    private Callbacks callbacks;
    private Exception error;

    public SetMessagesReadTask(String contactUsername, OkHttpClient okHttpClient, Callbacks callbacks) {
        this.contactUsername = contactUsername;
        this.okHttpClient = okHttpClient;
        this.callbacks = callbacks;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            String url = "https://www.codementor.io/api/chatrooms/" + contactUsername + "/read";
            Request request = new Request.Builder()
                    .post(RequestBody.create(MediaType.parse("text/plain"), ""))
                    .url(url)
                    .build();

            okHttpClient.newCall(request).execute();
        } catch (IOException | JsonSyntaxException e) {
            error = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (error != null) {
            callbacks.onError(error);
        }
    }

    public interface Callbacks {

        /**
         * Called when an error occurs.
         *
         * @param e The error's Exception.
         */
        void onError(Exception e);

    }

}
