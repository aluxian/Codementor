package com.aluxian.codementor.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluxian.codementor.App;
import com.aluxian.codementor.R;
import com.aluxian.codementor.adapters.ConversationAdapter;
import com.aluxian.codementor.models.Chatroom;
import com.google.gson.Gson;

public class ConversationFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String ARG_CHATROOM_JSON = "chatroom_json";

    private ConversationAdapter conversationAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static ConversationFragment newInstance(Chatroom chatroom) {
        Bundle args = new Bundle();
        args.putString(ARG_CHATROOM_JSON, new Gson().toJson(chatroom));

        ConversationFragment fragment = new ConversationFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Chatroom chatroom = new Gson().fromJson(getArguments().getString(ARG_CHATROOM_JSON), Chatroom.class);
        View rootView = inflater.inflate(R.layout.fragment_chatroom, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(layoutManager);

        App app = (App) getActivity().getApplication();
        conversationAdapter = new ConversationAdapter(chatroom, app.getUserManager(), getActivity());
        recyclerView.setAdapter(conversationAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.sunset_orange));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.postDelayed(() -> {
            mSwipeRefreshLayout.setRefreshing(true);
            onRefresh();
        }, 250);
    }

    @Override
    public void onRefresh() {
        conversationAdapter.refresh(() -> {
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(false);
        });
    }

}
