package com.aluxian.codementor.presentation.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.aluxian.codementor.MessageFieldTextWatcher;
import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.presentation.adapters.ConversationAdapter;
import com.aluxian.codementor.presentation.presenters.ConversationPresenter;
import com.aluxian.codementor.presentation.views.ConversationView;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;

public class ConversationFragment extends BaseFragment<ConversationPresenter> implements ConversationView {

    private static final String ARG_CHATROOM_JSON = "chatroom_json";
    private ConversationAdapter conversationAdapter;

    @Bind(R.id.btn_send) ImageButton sendButton;
    @Bind(R.id.tv_message) EditText messageField;
    @Bind(R.id.recycler) RecyclerView recyclerView;
    @Bind(R.id.swipe_container) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.tv_empty_state) TextView emptyState;

    public static ConversationFragment newInstance(Chatroom chatroom) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CHATROOM_JSON, chatroom);

        ConversationFragment fragment = new ConversationFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationAdapter = new ConversationAdapter();
        conversationAdapter.setHasStableIds(true);

        Chatroom chatroom = (Chatroom) getArguments().getSerializable(ARG_CHATROOM_JSON);
        setPresenter(new ConversationPresenter(this, getCoreServices(), chatroom, conversationAdapter));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_conversation, container, false);
        ButterKnife.bind(this, rootView);

        recyclerView.setAdapter(conversationAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), VERTICAL, true));

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.brand_accent));
        swipeRefreshLayout.setEnabled(false);

        messageField.requestFocus();
        messageField.setOnFocusChangeListener(this::onFocusChanged);

        int brandAccentColor = ContextCompat.getColor(getContext(), R.color.brand_accent);
        int sendButtonDisabledColor = ContextCompat.getColor(getContext(), R.color.neutral_gray);

        sendButton.setOnClickListener(this::onSendClicked);
        sendButton.setColorFilter(sendButtonDisabledColor, PorterDuff.Mode.SRC_ATOP);
        sendButton.setClickable(false);

        messageField.addTextChangedListener(new MessageFieldTextWatcher(
                sendButtonDisabledColor, brandAccentColor, sendButton));

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.post(() -> {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(refreshing);
                }
            });
        }
    }

    @Override
    public void showEmptyState(boolean show) {
        emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setSubtitle(@Nullable String subtitle) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(subtitle);
        }
    }

    @Override
    public void setSubtitle(int stringId) {
        setSubtitle(getString(stringId));
    }

    @Override
    public void setMessageFieldText(String text) {
        messageField.setText(text);
    }

    private void onFocusChanged(View view, boolean hasFocus) {
        if (!hasFocus) {
            InputMethodManager inputMethodManager = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void onSendClicked(View buttonView) {
        getPresenter().sendMessage(messageField.getText().toString().trim());
    }

}
