package com.aluxian.codementor.presentation.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.ConversationItem;
import com.aluxian.codementor.data.models.Message;
import com.aluxian.codementor.data.models.TimeMarker;
import com.aluxian.codementor.presentation.holders.ConversationItemViewHolder;
import com.aluxian.codementor.presentation.holders.MessageViewHolder;
import com.aluxian.codementor.presentation.holders.TimeMarkerViewHolder;
import com.aluxian.codementor.services.CoreServices;
import com.aluxian.codementor.services.ErrorHandler;
import com.aluxian.codementor.tasks.CodementorTasks;
import com.aluxian.codementor.tasks.FirebaseTasks;
import com.aluxian.codementor.utils.Helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import static com.aluxian.codementor.utils.Constants.UI;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationItemViewHolder> {

    private TreeSet<Message> messages = new TreeSet<>();
    private List<ConversationItem> items = new ArrayList<>();

    private Chatroom chatroom;
    private CodementorTasks codementorTasks;
    private FirebaseTasks firebaseTasks;
    private ErrorHandler errorHandler;

    public ConversationAdapter(Chatroom chatroom, CoreServices coreServices) {
        this.chatroom = chatroom;
        this.codementorTasks = coreServices.getCodementorTasks();
        this.firebaseTasks = coreServices.getFirebaseTasks();
        this.errorHandler = coreServices.getErrorHandler();
    }

    @Override
    public ConversationItemViewHolder onCreateViewHolder(ViewGroup parent, int layoutId) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);

        if (layoutId == R.layout.item_time_marker) {
            return new TimeMarkerViewHolder(rootView);
        } else {
            return new MessageViewHolder(rootView);
        }
    }

    @Override
    public void onBindViewHolder(ConversationItemViewHolder holder, int position) {
        ConversationItem item = items.get(position);
        boolean isNewest = position == 0;

        if (isNewest && !item.sentByMe() && !item.isRead() && item instanceof Message) {
            firebaseTasks.markRead(chatroom, ((Message) item))
                    .onSuccessTask(task -> codementorTasks.markConversationRead(chatroom))
                    .continueWith(errorHandler::logAndToastTask, UI);
        }

        holder.bindItem(item, isNewest);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getLayoutId();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public Message getOldestMessage() {
        return messages.first();
    }

    public void addNewMessages(TreeSet<Message> newMessages) {
        if (messages.size() > 0 && newMessages.size() > 0 && messages.last().equals(newMessages.last())) {
            return;
        }

        TreeSet<Message> existingMessages = new TreeSet<>(messages);
        messages.clear();
        messages.addAll(newMessages);
        messages.addAll(existingMessages);
        generateItems();
    }

    public void addOldMessages(TreeSet<Message> oldMessages) {
        messages.addAll(oldMessages);
        generateItems();
    }

    private void generateItems() {
        items.clear();
        Message message1 = null;

        for (Message message2 : messages) {
            if (message1 == null) {
                long firstTimestamp = message2.getTimestamp();
                items.add(new TimeMarker(firstTimestamp));

                items.add(message2);
                message1 = message2;

                continue;
            }

            long timestamp1 = message1.getTimestamp();
            long timestamp2 = message2.getTimestamp();

            if (!Helpers.isSameDay(timestamp1, timestamp2)) {
                items.add(new TimeMarker(timestamp2));
            }

            items.add(message2);
            message1 = message2;
        }

        Collections.reverse(items);
        notifyDataSetChanged();
    }

}
