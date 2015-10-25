package com.aluxian.codementor.presentation.adapters;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.presentation.holders.ChatroomViewHolder;
import com.aluxian.codementor.presentation.listeners.ChatroomSelectedListener;
import com.aluxian.codementor.services.CoreServices;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatroomsAdapter extends RecyclerView.Adapter<ChatroomViewHolder> {

    private List<Chatroom> chatrooms = new ArrayList<>();
    private @Nullable ChatroomSelectedListener chatroomSelectedListener;
    private CoreServices coreServices;

    public ChatroomsAdapter(CoreServices coreServices) {
        this.coreServices = coreServices;
    }

    @Override
    public long getItemId(int position) {
        String chatroomId = chatrooms.get(position).getId();
        return UUID.fromString(chatroomId).getMostSignificantBits();
    }

    @Override
    public ChatroomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatroom, parent, false);
        return new ChatroomViewHolder(rootView, coreServices);
    }

    @Override
    public void onBindViewHolder(ChatroomViewHolder holder, int position) {
        Chatroom chatroom = chatrooms.get(position);
        holder.loadChatroom(chatroom);
        holder.itemView.setOnClickListener(v -> {
            if (chatroomSelectedListener != null) {
                chatroomSelectedListener.onChatroomSelected(chatroom);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatrooms.size();
    }

    public void setChatroomSelectedListener(@Nullable ChatroomSelectedListener chatroomSelectedListener) {
        this.chatroomSelectedListener = chatroomSelectedListener;
    }

    public void updateList(List<Chatroom> chatrooms) {
        this.chatrooms = chatrooms;
        notifyDataSetChanged();
    }

}
