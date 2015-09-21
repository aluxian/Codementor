package com.aluxian.codementor.tasks;

import android.os.AsyncTask;

import com.aluxian.codementor.models.Chatroom;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class GetChatroomsTask extends AsyncTask<Void, Void, List<Chatroom>> {

    private OkHttpClient okHttpClient;
    private Callbacks callbacks;

    public GetChatroomsTask(OkHttpClient okHttpClient, Callbacks callbacks) {
        this.okHttpClient = okHttpClient;
        this.callbacks = callbacks;
    }

    @Override
    protected List<Chatroom> doInBackground(Void... params) {
        try {
            Request request = new Request.Builder().url("https://www.codementor.io/api/chatrooms").build();
            Response response = okHttpClient.newCall(request).execute();
            String responseBody = response.body().string();

            Type listType = new TypeToken<List<Chatroom>>() {}.getType();
            return new Gson().fromJson(responseBody, listType);
        } catch (IOException | JsonSyntaxException e) {
            callbacks.onError(e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Chatroom> chatrooms) {
        super.onPostExecute(chatrooms);

        if (chatrooms != null) {
            callbacks.onFinishedLoading(chatrooms);
        }

        callbacks.onFinished();
    }

    public interface Callbacks {

        /**
         * Called *only* when the task finished successfully.
         *
         * @param chatrooms The list of chatrooms.
         */
        void onFinishedLoading(List<Chatroom> chatrooms);

        /**
         * Called when the task completes execution.
         */
        void onFinished();

        /**
         * Called when an error occurs.
         *
         * @param e The error's Exception.
         */
        void onError(Exception e);

    }

}
