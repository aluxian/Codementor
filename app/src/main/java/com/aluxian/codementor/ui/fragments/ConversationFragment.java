package com.aluxian.codementor.ui.fragments;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.presentation.adapters.ConversationAdapter;
import com.aluxian.codementor.presentation.listeners.EndlessRecyclerScrollListener;
import com.aluxian.codementor.presentation.presenters.ConversationPresenter;
import com.aluxian.codementor.presentation.views.ConversationView;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;

public class ConversationFragment extends BaseFragment<ConversationPresenter> implements ConversationView {

    private static final String ARG_CHATROOM_JSON = "chatroom_json";

    private ConversationAdapter conversationAdapter;
    private boolean allMessagesLoaded;

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
        setPresenter(new ConversationPresenter(this, conversationAdapter, chatroom, getCoreServices()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_conversation, container, false);
        ButterKnife.bind(this, rootView);

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.brand_accent));
        swipeRefreshLayout.setEnabled(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), VERTICAL, true);
        ScrollListener scrollListener = new ScrollListener(layoutManager);

        recyclerView.setAdapter(conversationAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(scrollListener);

        int accentColor = ContextCompat.getColor(getContext(), R.color.brand_accent);
        int disabledColor = ContextCompat.getColor(getContext(), R.color.neutral_gray);

        sendButton.setOnClickListener(this::onSendClicked);
        sendButton.setColorFilter(disabledColor, PorterDuff.Mode.SRC_ATOP);
        sendButton.setClickable(false);

        messageField.requestFocus();
        messageField.setOnFocusChangeListener(this::onFocusChanged);
        messageField.addTextChangedListener(new MessageFieldTextWatcher(disabledColor, accentColor, sendButton));

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
        if (emptyState != null) {
            emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        }
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

    @Override
    public void setAllMessagesLoaded(boolean loaded) {
        allMessagesLoaded = loaded;
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

    private class ScrollListener extends EndlessRecyclerScrollListener {

        public ScrollListener(LinearLayoutManager layoutManager) {
            super(layoutManager);
        }

        @Override
        public void loadMore() {
            getPresenter().loadMore();
        }

        @Override
        public boolean isFullyLoaded() {
            return allMessagesLoaded;
        }

        @Override
        public boolean isLoading() {
            return swipeRefreshLayout.isRefreshing();
        }

    }

    private static class MessageFieldTextWatcher implements TextWatcher {

        private final int disabledColor;
        private final int accentColor;
        private final ImageButton sendButton;

        public MessageFieldTextWatcher(int disabledColor, int accentColor, ImageButton sendButton) {
            this.disabledColor = disabledColor;
            this.accentColor = accentColor;
            this.sendButton = sendButton;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                if (!sendButton.isClickable()) {
                    animateSendButton(disabledColor, accentColor);
                    sendButton.setClickable(true);
                }
            } else {
                if (sendButton.isClickable()) {
                    animateSendButton(accentColor, disabledColor);
                    sendButton.setClickable(false);
                }
            }
        }

        private void animateSendButton(int fromColor, int toColor) {
            final float[] from = new float[3];
            final float[] to = new float[3];

            Color.colorToHSV(fromColor, from);
            Color.colorToHSV(toColor, to);

            ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
            anim.setDuration(300);

            final float[] hsv = new float[3];
            anim.addUpdateListener(animation -> {
                hsv[0] = from[0] + (to[0] - from[0]) * animation.getAnimatedFraction();
                hsv[1] = from[1] + (to[1] - from[1]) * animation.getAnimatedFraction();
                hsv[2] = from[2] + (to[2] - from[2]) * animation.getAnimatedFraction();

                sendButton.setColorFilter(Color.HSVToColor(hsv), PorterDuff.Mode.SRC_ATOP);
            });

            anim.start();
        }

    }

}
