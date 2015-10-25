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
import com.aluxian.codementor.data.types.MessageType;
import com.aluxian.codementor.presentation.holders.HtmlMessageViewHolder;
import com.aluxian.codementor.presentation.holders.MessageViewHolder;
import com.aluxian.codementor.presentation.holders.TimeMarkerViewHolder;

import java.util.ArrayList;
import java.util.Calendar;
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
        MessageType type = MessageType.getByFlag(viewType);

        switch (type) {
            case MESSAGE:
                layoutId = alignRight ? R.layout.item_msg_text_right : R.layout.item_msg_text_left;
                rootView = LayoutInflater.from(context).inflate(layoutId, parent, false);
                return new MessageViewHolder(rootView);

            case FILE:
            case REQUEST:
                layoutId = alignRight ? R.layout.item_msg_html_right : R.layout.item_msg_html_left;
                rootView = LayoutInflater.from(context).inflate(layoutId, parent, false);
                return new HtmlMessageViewHolder(rootView);

            default:
                layoutId = alignRight ? R.layout.item_msg_system_right : R.layout.item_msg_system_left;
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
        } else if (holder instanceof HtmlMessageViewHolder) {
            HtmlMessageViewHolder htmlMessageViewHolder = (HtmlMessageViewHolder) holder;
            htmlMessageViewHolder.loadMessage(item.getMessage(), position == 0);
        } else {
            MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
            messageViewHolder.loadMessage(item.getMessage(), position == 0);
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

    public void addSentMessage(Message message) {
        items.add(0, new ConversationItem(message));
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

            long timestamp1 = message1.getCreatedAt();
            long timestamp2 = message2.getCreatedAt();

            Date date1 = new Date(timestamp1);
            Date date2 = new Date(timestamp2);

            if (!isSameDay(date1, date2)) {
                addTimeMarker(timestamp1);
            }

            addMessage(message2);
        }

        Message lastMessage = items.get(items.size() - 1).getMessage();
        long lastTimestamp = lastMessage.getCreatedAt();
        Date lastDate = new Date(lastTimestamp);

        if (!isSameDay(lastDate, new Date())) {
            addTimeMarker(lastTimestamp);
        }

        notifyDataSetChanged();
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        cal1.setTime(date1);
        cal2.setTime(date2);

        boolean sameYear = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

        return sameYear && sameDay;
    }

    private void addMessage(Message message) {
        items.add(new ConversationItem(message));
    }

    private void addTimeMarker(long timestamp) {
        items.add(new ConversationItem(new TimeMarker(timestamp)));
    }

}
