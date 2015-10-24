package com.aluxian.codementor.presentation.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.ConversationItem;
import com.aluxian.codementor.data.models.Message;
import com.aluxian.codementor.data.models.TimeMarker;
import com.aluxian.codementor.presentation.holders.FileMessageViewHolder;
import com.aluxian.codementor.presentation.holders.MessageViewHolder;
import com.aluxian.codementor.presentation.holders.TimeMarkerViewHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.aluxian.codementor.data.models.ConversationItem.TYPE_ALIGN_RIGHT;
import static com.aluxian.codementor.data.models.ConversationItem.TYPE_TIME_MARKER;

public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ConversationItem> items = new ArrayList<>();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        View rootView;
        int layoutId;

        if ((viewType & TYPE_TIME_MARKER) == TYPE_TIME_MARKER) {
            rootView = LayoutInflater.from(context).inflate(R.layout.item_time_marker, parent, false);
            return new TimeMarkerViewHolder(rootView);
        }

        boolean alignRight = (viewType & TYPE_ALIGN_RIGHT) == TYPE_ALIGN_RIGHT;
        Message.Type type = Message.Type.getByFlag(viewType);

        switch (type) {
            case MESSAGE:
                layoutId = alignRight ? R.layout.item_msg_text_right : R.layout.item_msg_text_left;
                rootView = LayoutInflater.from(context).inflate(layoutId, parent, false);
                return new MessageViewHolder(rootView);

            case FILE:
                layoutId = alignRight ? R.layout.item_msg_file_right : R.layout.item_msg_file_left;
                rootView = LayoutInflater.from(context).inflate(layoutId, parent, false);
                return new FileMessageViewHolder(rootView);

            default:
                layoutId = alignRight ? R.layout.item_msg_other_right : R.layout.item_msg_other_left;
                rootView = LayoutInflater.from(context).inflate(layoutId, parent, false);
                return new MessageViewHolder(rootView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ConversationItem item = items.get(position);

        if (holder instanceof TimeMarkerViewHolder) {
            TimeMarkerViewHolder timeMarkerViewHolder = (TimeMarkerViewHolder) holder;
            timeMarkerViewHolder.loadTimeMarker(item.getTimeMarker());
        } else if (holder instanceof FileMessageViewHolder) {
            FileMessageViewHolder fileMessageViewHolder = (FileMessageViewHolder) holder;
            fileMessageViewHolder.loadMessage(item.getMessage());
        } else {
            MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
            messageViewHolder.loadMessage(item.getMessage());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getViewType();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
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

        addMessage(messages.get(0));

        for (int i = 1; i < messages.size(); i++) {
            Message message1 = messages.get(i - 1);
            Message message2 = messages.get(i);

            long timestamp1 = normalizeTimestamp(message1.getCreatedAt());
            long timestamp2 = normalizeTimestamp(message2.getCreatedAt());

            if (timestamp1 - timestamp2 > 60 * 60 * 1000) {
                addTimeMarker(timestamp1);
            }

            addMessage(message2);
        }

        Message lastMessage = items.get(items.size() - 1).getMessage();
        long lastTimestamp = normalizeTimestamp(lastMessage.getCreatedAt());

        if (new Date().getTime() - lastTimestamp > 60 * 60 * 1000) {
            addTimeMarker(lastTimestamp);
        }

        notifyDataSetChanged();
    }

    private long normalizeTimestamp(long timestamp) {
        return new Date(timestamp).getTime();
    }

    private void addMessage(Message message) {
        items.add(new ConversationItem(message));
    }

    private void addTimeMarker(long timestamp) {
        items.add(new ConversationItem(new TimeMarker(timestamp)));
    }

}
