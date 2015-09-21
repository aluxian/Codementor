package com.aluxian.codementor.fragments;

import android.content.Context;
import android.content.Intent;
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
import com.aluxian.codementor.activities.LoginActivity;
import com.aluxian.codementor.adapters.ConversationAdapter;
import com.aluxian.codementor.models.Chatroom;
import com.aluxian.codementor.tasks.SendMessageTask;
import com.google.gson.Gson;

public class ConversationFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        ConversationAdapter.Callbacks, SendMessageTask.Callbacks {

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
        chatroom = new Gson().fromJson(getArguments().getString(ARG_CHATROOM_JSON), Chatroom.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chatroom, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(layoutManager);

        conversationAdapter = new ConversationAdapter(app.getUserManager(), this, chatroom);
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
                new SendMessageTask(app.getOkHttpClient(), app.getUserManager(), this, chatroom).execute(message);
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
        conversationAdapter.startRefresh();
    }

    @Override
    public void onRefreshFinished() {
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setEnabled(false);
    }

    @Override
    public void onPermissionDenied(Exception e) {
        onBackgroundError(new Exception("Permission denied from Firebase, please log in again", e));
        getActivity().startActivity(new Intent(getContext(), LoginActivity.class));
        getActivity().finish();
    }

    @Override
    public void onError(Exception e) {
        Log.e(TAG, e.getMessage(), e);
        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackgroundError(Exception e) {
        getActivity().runOnUiThread(() -> onError(e));
    }

}
