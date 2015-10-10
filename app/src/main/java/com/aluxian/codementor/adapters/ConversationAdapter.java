package com.aluxian.codementor.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.format.Formatter;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aluxian.codementor.R;
import com.aluxian.codementor.models.Chatroom;
import com.aluxian.codementor.models.Message;
import com.aluxian.codementor.models.TimeMarker;
import com.aluxian.codementor.tasks.FirebaseReAuthTask;
import com.aluxian.codementor.tasks.ParseFirebaseResponseTask;
import com.aluxian.codementor.tasks.SetMessagesReadTask;
import com.aluxian.codementor.utils.UserManager;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ValueEventListener, ParseFirebaseResponseTask.Callbacks,
        SetMessagesReadTask.Callbacks, FirebaseReAuthTask.Callbacks {

    private static final int ITEM_TYPE_TIME_MARKER = 0;
    private static final int ITEM_TYPE_EMPTY = 1;
    private static final int ITEM_TYPE_MESSAGE = 2;
    private static final int ITEM_TYPE_CONNECT = 3;
    private static final int ITEM_TYPE_FILE = 4;

    private Firebase firebaseRef;
    private Firebase chatroomFirebaseRef;

    private OkHttpClient okHttpClient;
    private UserManager userManager;
    private Callbacks callbacks;

    private int senderChatTextColor;
    private int senderChatBackgroundColor;
    private int defaultDarkTextColor;

    private List<Object> items = new ArrayList<>();
    private boolean retriedFirebaseAuth = false;
    private boolean showEmpty = false;

    public ConversationAdapter(Firebase firebaseRef, OkHttpClient okHttpClient, UserManager userManager,
                               Callbacks callbacks, Chatroom chatroom) {
        this.firebaseRef = firebaseRef;
        this.chatroomFirebaseRef = firebaseRef.child(chatroom.getFirebasePath());
        this.okHttpClient = okHttpClient;
        this.userManager = userManager;
        this.callbacks = callbacks;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Context context = recyclerView.getContext();

        senderChatTextColor = ContextCompat.getColor(context, R.color.sender_text);
        senderChatBackgroundColor = ContextCompat.getColor(context, R.color.sender_background);
        defaultDarkTextColor = -1;
    }

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

            default:
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empty, parent, false);
                return new EmptyViewHolder(rootView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EmptyViewHolder) {
            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
            emptyViewHolder.textView.setText(R.string.empty_conversation);
        } else if (holder instanceof TimeMarkerViewHolder) {
            TimeMarkerViewHolder timeMarkerViewHolder = (TimeMarkerViewHolder) holder;
            TimeMarker timeMarker = (TimeMarker) items.get(position);
            timeMarkerViewHolder.textView.setReferenceTime(timeMarker.getTimestamp());
        } else {
            MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
            Message message = ((Message) items.get(position));

            if (defaultDarkTextColor == -1) {
                defaultDarkTextColor = messageViewHolder.messageTextView.getCurrentTextColor();
            }

            String body = message.getTypeContent(userManager.getUsername());
            boolean alignRight = message.sentBy(userManager.getUsername());

            bindAdjustGravity(messageViewHolder, alignRight);
            bindAdjustColors(messageViewHolder, alignRight);

            if (messageViewHolder instanceof FileMessageViewHolder) {
                bindFileMessage((FileMessageViewHolder) messageViewHolder, message, body, alignRight);
            } else {
                messageViewHolder.messageTextView.setText(body);
            }

            messageViewHolder.messageTextView.requestLayout();
        }
    }

    private void bindFileMessage(FileMessageViewHolder holder, Message message, String body, boolean alignRight) {
        Spanned htmlBody = Html.fromHtml(body);
        holder.messageTextView.setText(htmlBody);

        String size = Formatter.formatShortFileSize(holder.itemView.getContext(), message.getRequest().getSize());
        holder.subtextView.setText(size);

        if (alignRight) {
            holder.subtextView.setGravity(Gravity.END);
            holder.subtextView.setTextColor(senderChatTextColor);
        } else {
            holder.subtextView.setGravity(Gravity.START);
            holder.subtextView.setTextColor(defaultDarkTextColor);
        }
    }

    private void bindAdjustGravity(MessageViewHolder holder, boolean alignRight) {
        int offset = holder.itemView.getResources().getDimensionPixelSize(R.dimen.message_offset);
        holder.linearLayout.setGravity(alignRight ? Gravity.END : Gravity.START);

        if (alignRight) {
            holder.messageTextView.setGravity(Gravity.END);
            holder.linearLayout.setPadding(offset, 0, 0, 0);
        } else {
            holder.messageTextView.setGravity(Gravity.START);
            holder.linearLayout.setPadding(0, 0, offset, 0);
        }
    }

    private void bindAdjustColors(MessageViewHolder holder, boolean alignRight) {
        View cardChild = holder.cardView.getChildAt(0);

        if (alignRight) {
            cardChild.setBackgroundColor(senderChatBackgroundColor);
            holder.messageTextView.setTextColor(senderChatTextColor);
        } else {
            cardChild.setBackgroundColor(Color.WHITE);
            holder.messageTextView.setTextColor(defaultDarkTextColor);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showEmpty) {
            return ITEM_TYPE_EMPTY;
        }

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

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        callbacks.onNewMessage();
        new ParseFirebaseResponseTask(this).execute(dataSnapshot);
        retriedFirebaseAuth = false;
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        if (firebaseError.getCode() == FirebaseError.PERMISSION_DENIED) {
            if (retriedFirebaseAuth) {
                callbacks.onRefreshFinished();
                callbacks.onPermissionDenied(firebaseError.toException());
                retriedFirebaseAuth = false;
            } else {
                new FirebaseReAuthTask(firebaseRef, okHttpClient, userManager, firebaseError, this).execute();
            }
        } else {
            callbacks.onRefreshFinished();
            callbacks.onError(firebaseError.toException());
        }
    }

    @Override
    public void onFirebaseResponse(List<Message> newMessages) {
        updateMessages(newMessages);
        showEmpty = newMessages.size() == 0;
        callbacks.onRefreshFinished();
        notifyDataSetChanged();

        if (newMessages.size() > 0) {
            Message lastMessage = newMessages.get(newMessages.size() - 1);

            if (!lastMessage.sentBy(userManager.getUsername()) && !lastMessage.hasBeenRead()) {
                new SetMessagesReadTask(lastMessage.getSender().getUsername(), okHttpClient, this).execute();
            }
        }
    }

    @Override
    public void onError(Exception e) {
        callbacks.onError(e);
    }

    private void updateMessages(List<Message> messages) {
        items.clear();

        if (messages.size() == 0) {
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
    }

    public void enableRefresh() {
        chatroomFirebaseRef.addValueEventListener(this);
    }

    public void disableRefresh() {
        chatroomFirebaseRef.removeEventListener(this);
    }

    @Override
    public void restartRefresh() {
        retriedFirebaseAuth = true;
        disableRefresh();
        enableRefresh();
    }

    @Override
    public void onFirebaseReAuthError(Exception e) {
        callbacks.onRefreshFinished();
        callbacks.onPermissionDenied(e);
    }

    public static class TimeMarkerViewHolder extends RecyclerView.ViewHolder {

        public final RelativeTimeTextView textView;

        public TimeMarkerViewHolder(View itemView) {
            super(itemView);
            textView = (RelativeTimeTextView) itemView;
        }

    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {

        public final TextView textView;

        public EmptyViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }

    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        public final CardView cardView;
        public final LinearLayout linearLayout;
        public final TextView messageTextView;

        public MessageViewHolder(View view) {
            super(view);
            cardView = (CardView) view.findViewById(R.id.card);
            linearLayout = (LinearLayout) view;
            messageTextView = (TextView) view.findViewById(R.id.message);
        }

    }

    public static class FileMessageViewHolder extends MessageViewHolder {

        public final TextView subtextView;

        public FileMessageViewHolder(View view) {
            super(view);
            subtextView = (TextView) view.findViewById(R.id.subtext);
            messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }

    }

    public interface Callbacks {

        /**
         * Called when an adapter refresh is finished.
         */
        void onRefreshFinished();

        /**
         * Called when a new message is received.
         */
        void onNewMessage();

        /**
         * Called when a PERMISSION_DENIED error is received from Firebase.
         *
         * @param e The error's Exception.
         */
        void onPermissionDenied(Exception e);

        /**
         * Called when an error occurs.
         *
         * @param e The error's Exception.
         */
        void onError(Exception e);

    }

}
