package com.aluxian.codementor.presentation.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Message;
import com.aluxian.codementor.data.models.TimeMarker;
import com.aluxian.codementor.presentation.holders.FileMessageViewHolder;
import com.aluxian.codementor.presentation.holders.MessageViewHolder;
import com.aluxian.codementor.presentation.holders.TimeMarkerViewHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE_TIME_MARKER = 1;
    private static final int ITEM_TYPE_MESSAGE = 2;
    private static final int ITEM_TYPE_CONNECT = 3;
    private static final int ITEM_TYPE_FILE = 4;

    private List<Object> items = new ArrayList<>();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView;

        switch (viewType) {
            case ITEM_TYPE_TIME_MARKER:
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_marker, parent, false);
                return new TimeMarkerViewHolder(rootView);

            case ITEM_TYPE_MESSAGE:
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_text, parent, false);
                return new MessageViewHolder(rootView);

            case ITEM_TYPE_CONNECT:
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_connect, parent, false);
                return new MessageViewHolder(rootView);

            case ITEM_TYPE_FILE:
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_file, parent, false);
                return new FileMessageViewHolder(rootView);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TimeMarkerViewHolder) {
            ((TimeMarkerViewHolder) holder).loadTimeMarker((TimeMarker) items.get(position));
        } else if (holder instanceof FileMessageViewHolder) {
            ((FileMessageViewHolder) holder).loadMessage((Message) items.get(position));
        } else {
            ((MessageViewHolder) holder).loadMessage((Message) items.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof TimeMarker) {
            return ITEM_TYPE_TIME_MARKER;
        }

        switch (((Message) items.get(position)).getType()) {
            case CONNECT:
                return ITEM_TYPE_CONNECT;

            case FILE:
                return ITEM_TYPE_FILE;

            default:
                return ITEM_TYPE_MESSAGE;
        }
    }

    @Override
    public long getItemId(int position) {
        if (items.get(position) instanceof TimeMarker) {
            return ((TimeMarker) items.get(position)).getTimestamp();
        } else {
            return UUID.fromString(((Message) items.get(position)).getId()).getMostSignificantBits();
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(List<Message> messages) {
        items.clear();

        if (messages.size() == 0) {
            notifyDataSetChanged();
            return;
        }

        items.add(messages.get(0));

        for (int i = 1; i < messages.size(); i++) {
            Message message1 = messages.get(i - 1);
            Message message2 = messages.get(i);

            long timestamp1 = new Date(message1.getCreatedAt()).getTime();
            long timestamp2 = new Date(message2.getCreatedAt()).getTime();

            if (timestamp1 - timestamp2 > 60 * 60 * 1000) {
                items.add(new TimeMarker(timestamp1));
            }

            items.add(message2);
        }

        Message lastMessage = (Message) items.get(items.size() - 1);
        long lastTimestamp = new Date(lastMessage.getCreatedAt()).getTime();

        if (new Date().getTime() - lastTimestamp > 60 * 60 * 1000) {
            items.add(new TimeMarker(lastTimestamp));
        }

        notifyDataSetChanged();
    }

}
