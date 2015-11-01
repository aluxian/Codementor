package com.aluxian.codementor.data.models;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Formatter;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.types.MessageType;
import com.aluxian.codementor.utils.Constants;
import com.aluxian.codementor.utils.Helpers;

import java.io.Serializable;

public class Message extends ConversationItem implements Serializable {

    private MessageData messageData;
    private String loggedInUsername;

    private long id;
    private MessageType type;
    private String displayText;
    private long createdAt;

    public Message(MessageData messageData, String loggedInUsername) {
        this.messageData = messageData;
        this.loggedInUsername = loggedInUsername;
    }

    @Override
    public long getId() {
        if (id == 0) {
            id = Helpers.parseStringId(messageData.getId());
        }

        return id;
    }

    @Override
    public int getLayoutId() {
        if (sentByMe()) {
            return getType().rightLayoutId;
        } else {
            return getType().leftLayoutId;
        }
    }

    @Override
    public long getTimestamp() {
        if (createdAt == 0) {
            createdAt = Helpers.parseDate(messageData.getCreatedAt());
        }

        return createdAt;
    }

    @Override
    public String getText() {
        if (displayText == null) {
            displayText = generateDisplayText();
        }

        return displayText;
    }

    @Override
    public String getSubtext(@Nullable Context context, boolean showSeen) {
        String subtext = "";
        long size = getSize();

        if (context != null) {
            // Time
            String time = DateUtils.formatDateTime(context, getTimestamp(), DateUtils.FORMAT_SHOW_TIME);
            subtext += time;

            // Size
            if (size > 0) {
                subtext += " " + Formatter.formatShortFileSize(context, size);
            }
        } else {
            // Time
            subtext += getTimestamp();

            // Size
            if (size > 0) {
                subtext += size;
            }
        }

        // Seen
        if (showSeen && sentByMe() && isRead()) {
            subtext += "  SEEN";
        }

        return subtext;
    }

    @Override
    public boolean isHtmlText() {
        return getType().leftLayoutId == R.layout.item_msg_html_left;
    }

    @Override
    public boolean isRead() {
        return !TextUtils.isEmpty(messageData.getReadAt());
    }

    @Override
    public boolean sentByMe() {
        return messageData.getSender().equals(getCurrentUser());
    }

    @Override
    public long getSize() {
        Request request = getRequest();

        if (request != null) {
            return request.getSize();
        }

        return 0;
    }

    public MessageType getType() {
        if (type == null) {
            type = MessageType.parse(messageData.getType());
        }

        return type;
    }

    public String getRawContent() {
        return messageData.getContent();
    }

    public Request getRequest() {
        return messageData.getRequest();
    }

    public User getCurrentUser() {
        User sender = messageData.getSender();
        User receiver = messageData.getReceiver();

        if (sender.getUsername().equals(loggedInUsername)) {
            return sender;
        } else {
            return receiver;
        }
    }

    public User getOtherUser() {
        User sender = messageData.getSender();
        User receiver = messageData.getReceiver();

        if (!sender.getUsername().equals(loggedInUsername)) {
            return sender;
        } else {
            return receiver;
        }
    }

    private String generateDisplayText() {
        switch (getType()) {
            case MESSAGE:
                return getRawContent();

            case CONNECT:
                if (sentByMe()) {
                    return "You requested to start a session.";
                }

                return getOtherUser().getShortestName() + " requested to start a session.";

            case FILE:
                String fileUrl = Helpers.escapeHtml(getRequest().getUrl());
                String fileText = Helpers.escapeHtml(getRequest().getFilename());
                return "<a href=\"" + fileUrl + "\">" + fileText + "</a>";

            case REQUEST:
                String attachedMessage = getOtherUser().getShortestName() + " attached a request: ";

                if (sentByMe()) {
                    attachedMessage = "You attached a request: ";
                }

                String reqUrl = Helpers.escapeHtml(Constants.requestUrl(getRequest().getId()));
                String reqText = Helpers.escapeHtml(getRequest().getTitle());
                String attachedText = Helpers.escapeHtml(attachedMessage);

                return "<i><b>" + attachedText + "</b></i><a href=\"" + reqUrl + "\">" + reqText + "</a>";

            case SIGNATURE:
                if (sentByMe()) {
                    return "You initiated a Non-Disclosure Agreement request.";
                }

                return getOtherUser().getShortestName() + " initiated a Non-Disclosure Agreement request.";

            default:
                return "Message type not yet supported by this app.";
        }
    }

}
