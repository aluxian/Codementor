package com.aluxian.codementor.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.aluxian.codementor.App;
import com.aluxian.codementor.R;
import com.aluxian.codementor.adapters.ConversationAdapter;
import com.aluxian.codementor.models.Chatroom;
import com.aluxian.codementor.models.Message;
import com.aluxian.codementor.models.User;
import com.aluxian.codementor.models.firebase.FirebaseMessage;
import com.aluxian.codementor.models.firebase.FirebaseServerMessage;
import com.aluxian.codementor.utils.CamelCaseNamingStrategy;
import com.firebase.client.Firebase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;

public class ConversationFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = ConversationFragment.class.getSimpleName();
    private static final String ARG_CHATROOM_JSON = "chatroom_json";

    private App app;
    private ConversationAdapter conversationAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Chatroom chatroom;

    public static ConversationFragment newInstance(Chatroom chatroom) {
        Bundle args = new Bundle();
        args.putString(ARG_CHATROOM_JSON, new Gson().toJson(chatroom));

        ConversationFragment fragment = new ConversationFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        app = (App) getActivity().getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chatroom, container, false);
        chatroom = new Gson().fromJson(getArguments().getString(ARG_CHATROOM_JSON), Chatroom.class);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(layoutManager);

        App app = (App) getActivity().getApplication();
        conversationAdapter = new ConversationAdapter(chatroom, app.getUserManager(), getActivity());
        conversationAdapter.setHasStableIds(true);
        recyclerView.setAdapter(conversationAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.sunset_orange));
        swipeRefreshLayout.setOnRefreshListener(this);

        ImageButton sendButton = (ImageButton) rootView.findViewById(R.id.send);
        EditText messageField = (EditText) rootView.findViewById(R.id.message);

        sendButton.setOnClickListener(v -> {
            String message = messageField.getText().toString();
            messageField.setText("");

            if (!TextUtils.isEmpty(message)) {
                new SendMessageTask().execute(message);
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.postDelayed(() -> {
            swipeRefreshLayout.setRefreshing(true);
            onRefresh();
        }, 250);
    }

    @Override
    public void onRefresh() {
        conversationAdapter.refresh(() -> {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(false);
        });
    }

    private class SendMessageTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            Message newestItem = conversationAdapter.getNewestItem();

            if (newestItem == null) {
                throw new RuntimeException("Must have an item to use as example for building a new request");
            }

            User receiver = chatroom.getOtherUser(app.getUserManager().getUsername());
            User sender = chatroom.getOtherUser(receiver.getUsername());

            FirebaseMessage firebaseMessage = new FirebaseMessage(chatroom.getChatroomId(),
                    Message.Type.MESSAGE, params[0], sender, receiver);

            Firebase ref = new Firebase("https://codementor.firebaseio.com/");
            ref.child(chatroom.getFirebasePath()).push().setValue(firebaseMessage, (firebaseError, msgRef) -> {
                if (firebaseError != null) {
                    Log.e(TAG, firebaseError.getMessage(), firebaseError.toException());
                    showErrorToast();
                    return;
                }

                Log.d(TAG, "Successfully saved on Firebase");
                Log.e(TAG, "FB " + msgRef.getKey() + "=key " + msgRef.getPath() + "=path str=" + msgRef.toString());

                try {
                    saveOnServer(firebaseMessage, msgRef.getKey());
                    Log.d(TAG, "Successfully saved on Codementor's server");
                } catch (IOException e) {
                    showErrorToast();
                    Log.e(TAG, e.getMessage(), e);
                }
            });

            return null;
        }

        private boolean saveOnServer(FirebaseMessage firebaseMessage, String firebaseKey) throws IOException {
            FirebaseServerMessage firebaseServerMessage = new FirebaseServerMessage(firebaseKey, firebaseMessage);
            String requestBody = new GsonBuilder()
                    .setFieldNamingStrategy(new CamelCaseNamingStrategy())
                    .create()
                    .toJson(firebaseServerMessage);

            Request request = new Request.Builder()
                    .post(RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), requestBody))
                    .url("https://www.codementor.io/api/chatrooms/" + app.getUserManager().getUsername())
                    .build();

            Log.e(TAG, "POST ON SERVER JSON=" + requestBody);
            return false;//app.getOkHttpClient().newCall(request).execute().isSuccessful();
        }

        private void showErrorToast() {
            Toast.makeText(getContext(), "An unexpected error occurred", Toast.LENGTH_SHORT).show();
        }

    }

}
