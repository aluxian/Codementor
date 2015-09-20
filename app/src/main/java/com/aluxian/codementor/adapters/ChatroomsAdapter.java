package com.aluxian.codementor.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aluxian.codementor.R;
import com.aluxian.codementor.lib.TrackSelectionAdapter;
import com.aluxian.codementor.models.Chatroom;
import com.aluxian.codementor.models.User;
import com.aluxian.codementor.utils.UserManager;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChatroomsAdapter extends TrackSelectionAdapter<TrackSelectionAdapter.ViewHolder> {

    private static final String TAG = ChatroomsAdapter.class.getSimpleName();

    private static final int ITEM_TYPE_CHATROOM = 1;
    private static final int ITEM_TYPE_EMPTY = 2;

    private final List<Chatroom> mChatroomsList = new ArrayList<>();
    private boolean showEmpty = false;

    private OkHttpClient okHttpClient;
    private UserManager userManager;
    private ClickListener clickListener;

//    private ColorStateList initialTitleColors;
//    private ColorStateList initialSubtitleColors;
//    private int selectedTitleColor;
//    private int selectedSubtitleColor;

    public ChatroomsAdapter(OkHttpClient okHttpClient, UserManager userManager, ClickListener clickListener) {
        this.okHttpClient = okHttpClient;
        this.userManager = userManager;
        this.clickListener = clickListener;
    }

    @Override
    public TrackSelectionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView;

        switch (viewType) {
            case ITEM_TYPE_CHATROOM:
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatroom, parent, false);
                return new ChatroomViewHolder(rootView);

            case ITEM_TYPE_EMPTY:
            default:
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empty, parent, false);
                return new EmptyViewHolder(rootView);
        }
    }

    @Override
    public void onBindViewHolder(TrackSelectionAdapter.ViewHolder holder, int position) {
        if (holder instanceof EmptyViewHolder) {
            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
            emptyViewHolder.textView.setText(R.string.empty_chatrooms);
        } else {
            ChatroomViewHolder chatroomViewHolder = (ChatroomViewHolder) holder;
            chatroomViewHolder.itemView.setOnClickListener(v -> {
                clickListener.onChatroomClick(position, mChatroomsList.get(position));
                chatroomViewHolder.handleSelectableClick();
            });

            Chatroom chatroom = mChatroomsList.get(position);
            User otherUser = chatroom.getOtherUser(userManager.getUsername());

            chatroomViewHolder.titleTextView.setText(otherUser.getName());

            if (chatroom.getSender().getUsername().equals(userManager.getUsername())) {
                String content = "You: " + chatroom.getContent();
                chatroomViewHolder.subtitleTextView.setText(content);
            } else {
                chatroomViewHolder.subtitleTextView.setText(chatroom.getContent());
            }

//            if (isSelected(position)) {
//                chatroomViewHolder.titleTextView.setTextColor(selectedTitleColor);
//                chatroomViewHolder.subtitleTextView.setTextColor(selectedSubtitleColor);
//            } else {
//                chatroomViewHolder.titleTextView.setTextColor(initialTitleColors);
//                chatroomViewHolder.subtitleTextView.setTextColor(initialSubtitleColors);
//            }

            Uri avatarUri = Uri.parse(otherUser.getAvatarUrl());
            chatroomViewHolder.avatarView.setImageURI(avatarUri);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return showEmpty ? ITEM_TYPE_EMPTY : ITEM_TYPE_CHATROOM;
    }

    @Override
    public int getItemCount() {
        return showEmpty ? 1 : mChatroomsList.size();
    }

    public void refresh(Runnable callback) {
        new GetChatroomsTask(callback).execute();
    }

    public class EmptyViewHolder extends TrackSelectionAdapter.ViewHolder {

        public final TextView textView;

        public EmptyViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }

    }

    public class ChatroomViewHolder extends TrackSelectionAdapter.ViewHolder {

        public final SimpleDraweeView avatarView;
        public final TextView titleTextView;
        public final TextView subtitleTextView;

        public ChatroomViewHolder(View itemView) {
            super(itemView);
            avatarView = (SimpleDraweeView) itemView.findViewById(R.id.avatar);
            titleTextView = (TextView) itemView.findViewById(R.id.title);
            subtitleTextView = (TextView) itemView.findViewById(R.id.subtitle);

//            initialTitleColors = titleTextView.getTextColors();
//            initialSubtitleColors = subtitleTextView.getTextColors();
//
//            selectedTitleColor = ContextCompat.getColor(itemView.getContext(), R.color.sunset_orange);
//            selectedSubtitleColor = ContextCompat.getColor(itemView.getContext(), R.color.sunset_orange_light);
        }

    }

    /**
     * Listen for click events on this adapter's views.
     */
    public interface ClickListener {

        /**
         * Called when a Chatroom is clicked in the adapter.
         *
         * @param position The position of the Chatroom in the list.
         * @param chatroom The Chatroom object.
         */
        void onChatroomClick(int position, Chatroom chatroom);

    }

    private class GetChatroomsTask extends AsyncTask<Void, Void, List<Chatroom>> {

        private Runnable callback;

        private GetChatroomsTask(Runnable callback) {
            this.callback = callback;
        }

        @Override
        protected List<Chatroom> doInBackground(Void... params) {
            try {
                Request request = new Request.Builder().url("https://www.codementor.io/api/chatrooms").build();
                Response response = okHttpClient.newCall(request).execute();
                String responseBody = response.body().string();

                Type listType = new TypeToken<List<Chatroom>>() {}.getType();
                return new Gson().fromJson(responseBody, listType);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Chatroom> chatrooms) {
            super.onPostExecute(chatrooms);

            if (chatrooms != null) {
                mChatroomsList.clear();
                mChatroomsList.addAll(chatrooms);
                showEmpty = mChatroomsList.size() == 0;
                notifyDataSetChanged();
            }

            callback.run();
        }

    }

}
