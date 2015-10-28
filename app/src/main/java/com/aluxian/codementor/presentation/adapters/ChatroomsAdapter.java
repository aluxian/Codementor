package com.aluxian.codementor.presentation.adapters;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.presentation.holders.ChatroomItemViewHolder;
import com.aluxian.codementor.presentation.listeners.ChatroomSelectedListener;

public class ChatroomsAdapter extends RecyclerView.Adapter<ChatroomItemViewHolder> {

    private SortedList<Chatroom> chatrooms;
    private ChatroomSelectedListener chatroomSelectedListener;

    public ChatroomsAdapter(ChatroomSelectedListener chatroomSelectedListener) {
        this.chatroomSelectedListener = chatroomSelectedListener;
    }

    public void setList(SortedList<Chatroom> chatrooms) {
        this.chatrooms = chatrooms;
    }

    @Override
    public long getItemId(int position) {
        return chatrooms.get(position).getId();
    }

    @Override
    public ChatroomItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatroom, parent, false);
        return new ChatroomItemViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(ChatroomItemViewHolder holder, int position) {
        Chatroom chatroom = chatrooms.get(position);
        holder.bindChatroom(chatroom);
        holder.itemView.setOnClickListener(v -> chatroomSelectedListener.onChatroomSelected(chatroom));
    }

    @Override
    public int getItemCount() {
        return chatrooms.size();
    }

}
