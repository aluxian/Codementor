package com.aluxian.codementor.models;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

@SuppressWarnings("unused")
public class Message {

    private static final String TAG = Message.class.getSimpleName();

    private String id;
    private String content;
    private User sender;
    private User receiver;
    private Request request;
    private String created_at;
    private String read_at;
    private String type;

    public Message() {}

    public String getContent() {
        return content;
    }

    public Request getRequest() {
        return request;
    }

    public long getCreatedAt() {
        return parseDate(created_at);
    }

    public Type getType() {
        String rawType = type.toUpperCase();

        if ("SESSIONLINK".equals(rawType)) {
            rawType = "CONNECT";
        }

        try {
            return Type.valueOf(rawType);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage(), e);
            return Type.MESSAGE;
        }
    }

    public User getMentor() {
        return sender.getRole().equals("mentor") ? sender : receiver;
    }

    public User getOtherUser(String loggedInUsername) {
        if (sender.getUsername().equals(loggedInUsername)) {
            return receiver;
        } else {
            return sender;
        }
    }

    public boolean hasBeenRead() {
        return read_at != null;
    }

    public boolean sentBy(String username) {
        return sender.getUsername().equals(username);
    }

    public String getTypeContent(String username) {
        switch (getType()) {
            case CONNECT:
                String paidMessageText = "You requested to start a paid session.";

                if (!sentBy(username)) {
                    paidMessageText = getMentor().getShortestName() + " requested to start a paid session.";
                }

                return paidMessageText;

            case FREECONNECT:
                String freeMessageText = "You requested to start a free session.";

                if (!sentBy(username)) {
                    freeMessageText = getMentor().getShortestName() + " requested to start a paid session.";
                }

                return freeMessageText;

            case FILE:
                return "<a href=\"" + request.getUrl() + "\">" + request.getFilename() + "</a>";

            default:
                return getContent();
        }
    }

    public String getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public enum Type {
        MESSAGE,
        CONNECT,
        FREECONNECT,
        FILE
    }

    private long parseDate(Object val) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH).parse((String) val).getTime();
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

        return id.equals(message.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
