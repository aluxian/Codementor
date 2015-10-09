package com.aluxian.codementor.tasks;

import android.os.AsyncTask;

import com.aluxian.codementor.models.Chatroom;
import com.aluxian.codementor.models.ChatroomList;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;

public class GetChatroomsTask extends AsyncTask<Void, Void, List<Chatroom>> {

    private OkHttpClient okHttpClient;
    private Callbacks callbacks;
    private Exception error;

    public GetChatroomsTask(OkHttpClient okHttpClient, Callbacks callbacks) {
        this.okHttpClient = okHttpClient;
        this.callbacks = callbacks;
    }

    @Override
    protected List<Chatroom> doInBackground(Void... params) {
        try {
            Request request = new Request.Builder().url("https://www.codementor.io/api/chatrooms/list").build();
            Response response = okHttpClient.newCall(request).execute();
            String responseBody = response.body().string();

            ChatroomList chatroomList = new Gson().fromJson(responseBody, ChatroomList.class);
            return chatroomList.getRecentChats();
        } catch (IOException | JsonSyntaxException e) {
            error = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Chatroom> chatrooms) {
        super.onPostExecute(chatrooms);

        if (error != null) {
            callbacks.onError(error);
        }

        if (chatrooms != null) {
            callbacks.onFinishedLoading(chatrooms);
        }

        callbacks.onFinished();
    }

    public interface Callbacks {

        /**
         * Called *only* when the task finished successfully.
         *
         * @param newChatrooms The list of chatrooms.
         */
        void onFinishedLoading(List<Chatroom> newChatrooms);

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
