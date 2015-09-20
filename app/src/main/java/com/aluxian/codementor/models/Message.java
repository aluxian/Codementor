package com.aluxian.codementor.models;

import android.util.Log;

import com.google.gson.JsonElement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Message {

    private static final String TAG = Message.class.getSimpleName();

    private String id;
    private String content;
    private User sender;
    private User receiver;
    private Request request;
    private String created_at;
    private String type;
    private JsonElement rawJson;

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
        try {
            return Type.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage(), e);
            return Type.MESSAGE;
        }
    }

    public User getMentor() {
        return sender.getRole().equals("mentor") ? sender : receiver;
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

    public JsonElement getRawJson() {
        return rawJson;
    }

    public void setRawJson(JsonElement rawJson) {
        this.rawJson = rawJson;
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

}
