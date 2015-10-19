package com.aluxian.codementor.data.models;

import android.text.Html;

import com.aluxian.codementor.utils.ErrorHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

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

        type = parseType();
        createdAt = parseDate();
        typeContent = parseTypeContent();
    }

    public String getId() {
        return messageData.id;
    }

    public User getSender() {
        return messageData.sender;
    }

    public User getReceiver() {
        return messageData.receiver;
    }

    public String getContent() {
        return messageData.content;
    }

    public Request getRequest() {
        return messageData.request;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Type getType() {
        return type;
    }

    public int getViewType() {
        boolean alignLeft = getSender().equals(getOtherUser());
        return getType().id * 10 + (alignLeft ? 1 : 2);
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
        return messageData.read_at != null;
    }

    public boolean sentByCurrentUser() {
        return getSender().equals(getCurrentUser());
    }

    public String getTypeContent() {
        return typeContent;
    }

    public enum Type {
        MESSAGE(1), CONNECT(2), FILE(3), SIGNATURE(4), OTHER(5);

        private final int id;

        Type(int id) {
            this.id = id;
        }

        public static Type getByid(int id) {
            switch (id) {
                case 1:
                    return MESSAGE;
                case 2:
                    return CONNECT;
                case 3:
                    return FILE;
                case 4:
                    return SIGNATURE;
                default:
                    return OTHER;
            }
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
        String rawType = messageData.type.toUpperCase();

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
        Object val = messageData.created_at;

        try {
            return DATE_FORMAT.parse((String) val).getTime();
        } catch (ParseException e) {
            try {
                return Double.valueOf(String.valueOf(val)).longValue();
            } catch (Exception ex) {
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
