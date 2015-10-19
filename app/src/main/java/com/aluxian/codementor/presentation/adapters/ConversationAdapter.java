package com.aluxian.codementor.presentation.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Message;
import com.aluxian.codementor.presentation.holders.FileMessageViewHolder;
import com.aluxian.codementor.presentation.holders.MessageViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages = new ArrayList<>();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        View rootView;
        int layoutId;

        boolean alignLeft = viewType % 10 == 1;
        Message.Type type = Message.Type.getByid(viewType / 10);

        switch (type) {
            case MESSAGE:
                layoutId = alignLeft ? R.layout.item_msg_text_left : R.layout.item_msg_text_right;
                rootView = LayoutInflater.from(context).inflate(layoutId, parent, false);
                return new MessageViewHolder(rootView);

            case FILE:
                layoutId = alignLeft ? R.layout.item_msg_file_left : R.layout.item_msg_file_right;
                rootView = LayoutInflater.from(context).inflate(layoutId, parent, false);
                return new FileMessageViewHolder(rootView);

            default:
                layoutId = alignLeft ? R.layout.item_msg_other_left : R.layout.item_msg_other_right;
                rootView = LayoutInflater.from(context).inflate(layoutId, parent, false);
                return new MessageViewHolder(rootView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FileMessageViewHolder) {
            ((FileMessageViewHolder) holder).loadMessage(messages.get(position));
        } else {
            ((MessageViewHolder) holder).loadMessage(messages.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getViewType();
    }

    @Override
    public long getItemId(int position) {
        return UUID.fromString(messages.get(position).getId()).getMostSignificantBits();
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateList(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

}
