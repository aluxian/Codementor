package com.aluxian.codementor.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aluxian.codementor.App;
import com.aluxian.codementor.R;
import com.aluxian.codementor.models.Chatroom;
import com.aluxian.codementor.tasks.GetChatroomsTask;
import com.aluxian.codementor.views.ChatroomEntryView;
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

    private App app;
    private OkHttpClient okHttpClient;
    private Callbacks callbacks;

    public ChatroomsAdapter(App app, OkHttpClient okHttpClient, Callbacks callbacks) {
        this.app = app;
        this.okHttpClient = okHttpClient;
        this.callbacks = callbacks;
    }

    @Override
    public long getItemId(int position) {
        return UUID.fromString(chatrooms.get(position).getId()).getMostSignificantBits();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView;

        switch (viewType) {
            case ITEM_TYPE_CHATROOM:
                ChatroomEntryView chatroomEntryView = new ChatroomEntryView(parent.getContext());
                chatroomEntryView.setApp(app);
                return new RecyclerView.ViewHolder(chatroomEntryView) {};

            default:
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empty, parent, false);
                return new EmptyViewHolder(rootView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof EmptyViewHolder)) {
            Chatroom chatroom = chatrooms.get(position);
            holder.itemView.setOnClickListener(v -> callbacks.onChatroomSelected(chatroom));
            ((ChatroomEntryView) holder.itemView).loadChatroom(chatroom);
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
    public void onFinishedLoading(List<Chatroom> newChatrooms) {
        chatrooms = newChatrooms;
        showEmpty = chatrooms.size() == 0;
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
            textView.setText(R.string.empty_chatrooms);
        }

    }

    public interface Callbacks {

        /**
         * Called when adapter refresh is finished.
         */
        void onRefreshFinished();

        /**
         * Called when a Chatroom is clicked in the adapter.
         *
         * @param chatroom The Chatroom object.
         */
        void onChatroomSelected(Chatroom chatroom);

        /**
         * Called when an error occurs.
         *
         * @param e The error's Exception.
         */
        void onError(Exception e);

    }

}
