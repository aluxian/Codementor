package com.aluxian.codementor.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aluxian.codementor.R;
import com.aluxian.codementor.models.Chatroom;
import com.aluxian.codementor.models.User;
import com.aluxian.codementor.tasks.GetChatroomsTask;
import com.aluxian.codementor.utils.UserManager;
import com.facebook.drawee.view.SimpleDraweeView;
import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatroomsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements GetChatroomsTask.Callbacks {

    private static final int ITEM_TYPE_EMPTY = 1;
    private static final int ITEM_TYPE_CHATROOM = 2;

    private List<Chatroom> chatrooms = new ArrayList<>();
    private boolean showEmpty = false;

    private OkHttpClient okHttpClient;
    private UserManager userManager;
    private Callbacks callbacks;

    public ChatroomsAdapter(OkHttpClient okHttpClient, UserManager userManager, Callbacks callbacks) {
        this.okHttpClient = okHttpClient;
        this.userManager = userManager;
        this.callbacks = callbacks;
    }

    @Override
    public long getItemId(int position) {
        return UUID.fromString(chatrooms.get(position).getChatroomId()).getMostSignificantBits();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView;

        switch (viewType) {
            case ITEM_TYPE_CHATROOM:
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatroom, parent, false);
                return new ChatroomViewHolder(rootView);

            default:
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empty, parent, false);
                return new EmptyViewHolder(rootView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EmptyViewHolder) {
            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
            emptyViewHolder.textView.setText(R.string.empty_chatrooms);
        } else {
            Chatroom chatroom = chatrooms.get(position);
            User otherUser = chatroom.getOtherUser(userManager.getUsername());

            ChatroomViewHolder chatroomViewHolder = (ChatroomViewHolder) holder;
            chatroomViewHolder.itemView.setOnClickListener(v -> callbacks.onChatroomClick(position, chatroom));
            chatroomViewHolder.titleTextView.setText(otherUser.getName());

            if (chatroom.getSender().getUsername().equals(userManager.getUsername())) {
                String content = "You: " + chatroom.getContent();
                chatroomViewHolder.subtitleTextView.setText(content);
            } else {
                chatroomViewHolder.subtitleTextView.setText(chatroom.getContent());
            }

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
        return showEmpty ? 1 : chatrooms.size();
    }

    @Override
    public void onFinishedLoading(List<Chatroom> chatrooms) {
        this.chatrooms.clear();
        this.chatrooms.addAll(chatrooms);
        showEmpty = this.chatrooms.size() == 0;
        notifyDataSetChanged();
    }

    @Override
    public void onFinished() {
        callbacks.onRefreshFinished();
    }

    @Override
    public void onError(Exception e) {
        callbacks.onError(e);
    }

    public void startRefresh() {
        new GetChatroomsTask(okHttpClient, this).execute();
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder {

        public final TextView textView;

        public EmptyViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }

    }

    public class ChatroomViewHolder extends RecyclerView.ViewHolder {

        public final TextView titleTextView;
        public final TextView subtitleTextView;
        public final SimpleDraweeView avatarView;

        public ChatroomViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.title);
            subtitleTextView = (TextView) itemView.findViewById(R.id.subtitle);
            avatarView = (SimpleDraweeView) itemView.findViewById(R.id.avatar);
        }

    }

    public interface Callbacks {

        /**
         * Called when an adapter refresh is finished.
         */
        void onRefreshFinished();

        /**
         * Called when a Chatroom is clicked in the adapter.
         *
         * @param position The position of the Chatroom in the list.
         * @param chatroom The Chatroom object.
         */
        void onChatroomClick(int position, Chatroom chatroom);

        /**
         * Called when an error occurs.
         *
         * @param e The error's Exception.
         */
        void onError(Exception e);

    }

}
