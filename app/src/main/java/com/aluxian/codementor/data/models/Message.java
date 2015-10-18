package com.aluxian.codementor.data.models;

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

    public Message(MessageData messageData, ErrorHandler errorHandler, String loggedInUsername) {
        this.messageData = messageData;
        this.errorHandler = errorHandler;
        this.loggedInUsername = loggedInUsername;
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
        return parseDate(messageData.created_at);
    }

    public Type getType() {
        String rawType = messageData.type.toUpperCase();

        if ("SESSIONLINK".equals(rawType)) {
            rawType = "CONNECT";
        }

        try {
            return Type.valueOf(rawType);
        } catch (IllegalArgumentException e) {
            errorHandler.log(e);
            return Type.MESSAGE;
        }
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
        switch (getType()) {
            case CONNECT:
                if (sentByCurrentUser()) {
                    return "You requested to start a paid session.";
                }

                return getOtherUser().getShortestName() + " requested to start a paid session.";

            case FREECONNECT:
                if (sentByCurrentUser()) {
                    return "You requested to start a free session.";
                }

                return getOtherUser().getShortestName() + " requested to start a paid session.";

            case FILE:
                return "<a href=\"" + getRequest().getUrl() + "\">" + getRequest().getFilename() + "</a>";

            default:
                return getContent();
        }
    }

    public enum Type {
        MESSAGE,
        CONNECT,
        FREECONNECT,
        FILE
    }

    private long parseDate(Object val) {
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
