package com.aluxian.codementor.tasks;

import android.os.AsyncTask;

import com.aluxian.codementor.models.Message;
import com.firebase.client.DataSnapshot;
import com.firebase.client.GenericTypeIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ParseFirebaseResponseTask extends AsyncTask<DataSnapshot, Void, List<Message>> {

    private Callbacks callbacks;

    public ParseFirebaseResponseTask(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    protected List<Message> doInBackground(DataSnapshot... params) {
        Type listType = new TypeToken<List<Message>>() {}.getType();
        Gson gson = new Gson();

        Map<String, Object> data = params[0].getValue(new GenericTypeIndicator<Map<String, Object>>() {});
        List<Message> messages = gson.fromJson(gson.toJsonTree(data.values()), listType);

        Collections.sort(messages, (lhs, rhs) -> {
            if (lhs.getCreatedAt() < rhs.getCreatedAt()) {
                return 1;
            } else if (lhs.getCreatedAt() > rhs.getCreatedAt()) {
                return -1;
            } else {
                return 0;
            }
        });

        return messages;
    }

    @Override
    protected void onPostExecute(List<Message> messages) {
        super.onPostExecute(messages);
        callbacks.onFirebaseResponse(messages);
    }

    public interface Callbacks {

        /**
         * Called when the Firebase response has been parsed.
         *
         * @param newMessages The list of messages to show.
         */
        void onFirebaseResponse(List<Message> newMessages);

    }

}
