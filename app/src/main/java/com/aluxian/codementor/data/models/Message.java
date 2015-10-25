package com.aluxian.codementor.data.models;

import android.text.Html;

import com.aluxian.codementor.utils.ErrorHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.aluxian.codementor.data.models.ConversationItem.TYPE_CONNECT;
import static com.aluxian.codementor.data.models.ConversationItem.TYPE_FILE;
import static com.aluxian.codementor.data.models.ConversationItem.TYPE_MESSAGE;
import static com.aluxian.codementor.data.models.ConversationItem.TYPE_SIGNATURE;

public class Message {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);

    private MessageData messageData;
    private ErrorHandler errorHandler;
    private String loggedInUsername;

    private Type type;
    private long createdAt;
    private String typeContent;

    public Message(MessageData messageData, ErrorHandler errorHandler, String loggedInUsername) {
        this.messageData = messageData;
        this.errorHandler = errorHandler;
        this.loggedInUsername = loggedInUsername;
    }

    public Message(FirebaseMessage firebaseMessage, ErrorHandler errorHandler, String loggedInUsername) {
        this.messageData = new MessageData(
                firebaseMessage.getId(),
                firebaseMessage.getContent(),
                firebaseMessage.getSender(),
                firebaseMessage.getReceiver(),
                firebaseMessage.getRequest(),
                DATE_FORMAT.format(new Date()),
                firebaseMessage.getReadAt(),
                firebaseMessage.getType()
        );

        this.errorHandler = errorHandler;
        this.loggedInUsername = loggedInUsername;
    }

    public String getId() {
        return messageData.getId();
    }

    public User getSender() {
        return messageData.getSender();
    }

    public User getReceiver() {
        return messageData.getReceiver();
    }

    public String getContent() {
        return messageData.getContent();
    }

    public Request getRequest() {
        return messageData.getRequest();
    }

    public long getCreatedAt() {
        if (createdAt == 0) {
            createdAt = parseDate();
        }

        return createdAt;
    }

    public Type getType() {
        if (type == null) {
            type = parseType();
        }

        return type;
    }

    public User getCurrentUser() {
        if (getSender().getUsername().equals(loggedInUsername)) {
            return getSender();
        } else {
            return getReceiver();
        }
    }

    public User getOtherUser() {
        if (!getSender().getUsername().equals(loggedInUsername)) {
            return getSender();
        } else {
            return getReceiver();
        }
    }

    public boolean hasBeenRead() {
        return messageData.getReadAt() != null;
    }

    public boolean sentByCurrentUser() {
        return getSender().equals(getCurrentUser());
    }

    public String getTypeContent() {
        if (typeContent == null) {
            typeContent = parseTypeContent();
        }

        return typeContent;
    }

    public enum Type {
        MESSAGE(TYPE_MESSAGE),
        CONNECT(ConversationItem.TYPE_CONNECT),
        FILE(ConversationItem.TYPE_FILE),
        SIGNATURE(ConversationItem.TYPE_SIGNATURE),
        OTHER(ConversationItem.TYPE_OTHER);

        public final int typeFlag;

        Type(int typeFlag) {
            this.typeFlag = typeFlag;
        }

        public static Type getByFlag(int flag) {
            if ((TYPE_MESSAGE & flag) == TYPE_MESSAGE) return MESSAGE;
            if ((TYPE_CONNECT & flag) == TYPE_CONNECT) return CONNECT;
            if ((TYPE_FILE & flag) == TYPE_FILE) return FILE;
            if ((TYPE_SIGNATURE & flag) == TYPE_SIGNATURE) return SIGNATURE;
            return OTHER;
        }
    }

    private String parseTypeContent() {
        switch (getType()) {
            case MESSAGE:
                return getContent();

            case CONNECT:
                if (sentByCurrentUser()) {
                    return "You requested to start a session.";
                }

                return getOtherUser().getShortestName() + " requested to start a session.";

            case FILE:
                String url = escapeHtml(getRequest().getUrl());
                String text = escapeHtml(getRequest().getFilename());
                return "<a href=\"" + url + "\">" + text + "</a>";

            default:
                return "This message type is not yet supported.";
        }
    }

    private String escapeHtml(String html) {
        return Html.fromHtml(html).toString();
    }

    private Type parseType() {
        String rawType = messageData.getType().toUpperCase();

        if (rawType.equals("PENDING_MSG")) {
            rawType = "MESSAGE";
        }

        if (rawType.equals("SESSIONLINK") || rawType.contains("CONNECT")) {
            rawType = "CONNECT";
        }

        try {
            return Type.valueOf(rawType);
        } catch (IllegalArgumentException e) {
            errorHandler.log(e);
            return Type.OTHER;
        }
    }

    private long parseDate() {
        Object val = messageData.getCreatedAt();

        try {
            return Double.valueOf(String.valueOf(val)).longValue();
        } catch (NumberFormatException e1) {
            try {
                return DATE_FORMAT.parse((String) val).getTime();
            } catch (ParseException e2) {
                return 0;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;

        Message message = (Message) o;
        return !(messageData != null ? !messageData.equals(message.messageData) : message.messageData != null);

    }

    @Override
    public int hashCode() {
        return messageData != null ? messageData.hashCode() : 0;
    }

}
