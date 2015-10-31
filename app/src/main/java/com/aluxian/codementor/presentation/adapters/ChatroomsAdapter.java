package com.aluxian.codementor.presentation.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.presentation.holders.ChatroomItemViewHolder;
import com.aluxian.codementor.presentation.listeners.ChatroomSelectedListener;

import java.util.ArrayList;
import java.util.List;

public class ChatroomsAdapter extends RecyclerView.Adapter<ChatroomItemViewHolder> {

    private List<Chatroom> chatrooms = new ArrayList<>();
    private ChatroomSelectedListener chatroomSelectedListener;

    public ChatroomsAdapter(ChatroomSelectedListener chatroomSelectedListener) {
        this.chatroomSelectedListener = chatroomSelectedListener;
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
    public long getItemId(int position) {
        return chatrooms.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return chatrooms.size();
    }

    public boolean isNewestChatroom(Chatroom chatroom) {
        return chatrooms.indexOf(chatroom) == 0;
    }

    public void updateChatroom(Chatroom chatroom) {
        int index = chatrooms.indexOf(chatroom);
        chatrooms.set(index, chatroom);
        notifyItemChanged(index);
    }

    public void addAll(List<Chatroom> newChatrooms) {
        chatrooms = newChatrooms;
        notifyDataSetChanged();
    }

}
