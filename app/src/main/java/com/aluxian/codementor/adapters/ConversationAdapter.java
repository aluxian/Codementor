package com.aluxian.codementor.adapters;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.Formatter;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aluxian.codementor.R;
import com.aluxian.codementor.activities.LoginActivity;
import com.aluxian.codementor.models.Chatroom;
import com.aluxian.codementor.models.Message;
import com.aluxian.codementor.utils.UserManager;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("Convert2streamapi")
public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ValueEventListener {

    private static final String TAG = ConversationAdapter.class.getSimpleName();

    private static final int ITEM_TYPE_EMPTY = 1;
    private static final int ITEM_TYPE_MESSAGE = 2;
    private static final int ITEM_TYPE_CONNECT = 3;
    private static final int ITEM_TYPE_FILE = 4;

    private Runnable refreshCallback;
    private UserManager mUserManager;
    private List<Message> mMessagesList;
    private Chatroom mChatroom;
    private boolean showEmpty = false;
    private Activity activity;

    public ConversationAdapter(Chatroom chatroom, UserManager userManager, Activity activity) {
        this.activity = activity;
        mMessagesList = new ArrayList<>();
        mUserManager = userManager;
        mChatroom = chatroom;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView;

        switch (viewType) {
            case ITEM_TYPE_MESSAGE:
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
                return new MessageViewHolder(rootView);

            case ITEM_TYPE_CONNECT:
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_connect, parent, false);
                return new MessageViewHolder(rootView);

            case ITEM_TYPE_FILE:
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_file, parent, false);
                return new FileMessageViewHolder(rootView);

            case ITEM_TYPE_EMPTY:
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

            Message message = mMessagesList.get(position);
            String body = message.getTypeContent(mUserManager.getUsername());

            boolean alignRight = message.sentBy(mUserManager.getUsername());
            int offset = holder.itemView.getResources().getDimensionPixelSize(R.dimen.message_offset);

            if (messageViewHolder instanceof FileMessageViewHolder) {
                String size = Formatter.formatShortFileSize(holder.itemView.getContext(),
                        message.getRequest().getSize());

                FileMessageViewHolder fileMessageHolder = (FileMessageViewHolder) messageViewHolder;
                fileMessageHolder.messageTextView.setText(Html.fromHtml(body));
                fileMessageHolder.subtextView.setText(size);
                fileMessageHolder.subtextView.setMovementMethod(LinkMovementMethod.getInstance());
                fileMessageHolder.subtextView.requestLayout();

                if (alignRight) {
                    fileMessageHolder.messageTextView.setGravity(Gravity.END);
                    fileMessageHolder.subtextView.setGravity(Gravity.END);
                } else {
                    fileMessageHolder.messageTextView.setGravity(Gravity.START);
                    fileMessageHolder.subtextView.setGravity(Gravity.START);
                }
            } else {
                messageViewHolder.messageTextView.setText(body);
            }

            messageViewHolder.messageTextView.requestLayout();
            messageViewHolder.linearLayout.setGravity(alignRight ? Gravity.END : Gravity.START);

            if (alignRight) {
                messageViewHolder.linearLayout.setPadding(offset, 0, 0, 0);
            } else {
                messageViewHolder.linearLayout.setPadding(0, 0, offset, 0);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showEmpty) {
            return ITEM_TYPE_EMPTY;
        }

        switch (mMessagesList.get(position).getType()) {
            case CONNECT:
                return ITEM_TYPE_CONNECT;

            case FILE:
                return ITEM_TYPE_FILE;

            case MESSAGE:
            default:
                return ITEM_TYPE_MESSAGE;
        }
    }

    @Override
    public long getItemId(int position) {
        return UUID.fromString(mMessagesList.get(position).getId()).getMostSignificantBits();
    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    public void refresh(Runnable callback) {
        refreshCallback = callback;
        String conversationPath = "chatrooms/" + mChatroom.getChatroomFirebaseId() + "/" + mChatroom.getChatroomId();
        Firebase ref = new Firebase("https://codementor.firebaseio.com/");
        ref.child(conversationPath).addValueEventListener(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mMessagesList = parseData();

                Collections.sort(mMessagesList, (lhs, rhs) -> {
                    if (lhs.getCreatedAt() < rhs.getCreatedAt()) {
                        return 1;
                    } else if (lhs.getCreatedAt() > rhs.getCreatedAt()) {
                        return -1;
                    } else {
                        return 0;
                    }
                });

                showEmpty = mMessagesList.size() == 0;
                return null;
            }

            private List<Message> parseData() {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Message>>() {}.getType();

                Map<String, Object> result = dataSnapshot.getValue(new GenericTypeIndicator<Map<String, Object>>() {});
                JsonElement parsed = gson.toJsonTree(result.values());
                List<Message> messages = gson.fromJson(parsed, listType);

                for (int i = 0; i < messages.size(); i++) {
                    messages.get(i).setRawJson(parsed.getAsJsonArray().get(i));
                }

                return messages;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                notifyDataSetChanged();

                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            }
        }.execute();
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        if (refreshCallback != null) {
            refreshCallback.run();
        }

        if (firebaseError.getCode() == FirebaseError.PERMISSION_DENIED) {
            Toast.makeText(activity, "Permission denied from Firebase, please log in again", Toast.LENGTH_LONG).show();
            activity.startActivity(new Intent(activity, LoginActivity.class));
            activity.finish();
        } else {
            Toast.makeText(activity, firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
        }

        Log.e(TAG, firebaseError.getMessage(), firebaseError.toException());
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {

        public final TextView textView;

        public EmptyViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }

    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        public final LinearLayout linearLayout;
        public final TextView messageTextView;

        public MessageViewHolder(View view) {
            super(view);
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

}
