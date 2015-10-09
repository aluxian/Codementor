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
import com.aluxian.codementor.tasks.ParseFirebaseResponseTask;
import com.aluxian.codementor.utils.UserManager;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ValueEventListener, ParseFirebaseResponseTask.Callbacks {

    private static final int ITEM_TYPE_EMPTY = 1;
    private static final int ITEM_TYPE_MESSAGE = 2;
    private static final int ITEM_TYPE_CONNECT = 3;
    private static final int ITEM_TYPE_FILE = 4;

    private Firebase chatroomFirebaseRef;
    private UserManager userManager;
    private Callbacks callbacks;

    private int senderChatTextColor;
    private int senderChatBackgroundColor;
    private int defaultDarkTextColor;

    private List<Message> messages = new ArrayList<>();
    private boolean showEmpty = false;

    public ConversationAdapter(Firebase firebase, UserManager userManager, Callbacks callbacks, Chatroom chatroom) {
        this.chatroomFirebaseRef = firebase.child(chatroom.getFirebasePath());
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
        } else {
            MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
            Message message = messages.get(position);

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
        holder.subtextView.setMovementMethod(LinkMovementMethod.getInstance());
        holder.subtextView.requestLayout();

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

        switch (messages.get(position).getType()) {
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
        return UUID.fromString(messages.get(position).getId()).getMostSignificantBits();
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        callbacks.onNewMessage();
        new ParseFirebaseResponseTask(this).execute(dataSnapshot);
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        callbacks.onRefreshFinished();

        if (firebaseError.getCode() == FirebaseError.PERMISSION_DENIED) {
            callbacks.onPermissionDenied(firebaseError.toException());
        } else {
            callbacks.onError(firebaseError.toException());
        }
    }

    @Override
    public void onFirebaseResponse(List<Message> newMessages) {
        this.messages = newMessages;
        showEmpty = messages.size() == 0;
        callbacks.onRefreshFinished();
        notifyDataSetChanged();
    }

    public void enableRefresh() {
        chatroomFirebaseRef.addValueEventListener(this);
    }

    public void disableRefresh() {
        chatroomFirebaseRef.removeEventListener(this);
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
