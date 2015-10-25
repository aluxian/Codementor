package com.aluxian.codementor.data.models;

import android.text.Html;

import com.aluxian.codementor.data.types.MessageType;
import com.aluxian.codementor.services.ErrorHandler;
import com.aluxian.codementor.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);

    private MessageData messageData;
    private ErrorHandler errorHandler;
    private String loggedInUsername;

    private MessageType type;
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

    public MessageType getType() {
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
                String fileUrl = escapeHtml(getRequest().getUrl());
                String fileText = escapeHtml(getRequest().getFilename());
                return "<a href=\"" + fileUrl + "\">" + fileText + "</a>";

            case REQUEST:
                String attachedMessage = getOtherUser().getShortestName() + " attached a request: ";

                if (sentByCurrentUser()) {
                    attachedMessage = "You attached a request: ";
                }

                String reqUrl = escapeHtml(Constants.getRequestUrl(getRequest().getId()));
                String reqText = escapeHtml(getRequest().getTitle());
                String attachedText = escapeHtml(attachedMessage);

                return "<i><b>" + attachedText + "</b></i><a href=\"" + reqUrl + "\">" + reqText + "</a>";

            case SIGNATURE:
                if (sentByCurrentUser()) {
                    return "You initiated a Non-Disclosure Agreement request.";
                }

                return getOtherUser().getShortestName() + " initiated a Non-Disclosure Agreement request.";

            default:
                return "This message type is not yet supported.";
        }
    }

    private String escapeHtml(String html) {
        return Html.fromHtml(html).toString();
    }

    private MessageType parseType() {
        String rawType = messageData.getType().toUpperCase();

        if (rawType.equals("PENDING_MSG")) {
            rawType = "MESSAGE";
        }

        if (rawType.equals("SESSIONLINK") || rawType.contains("CONNECT")) {
            rawType = "CONNECT";
        }

        try {
            return MessageType.valueOf(rawType);
        } catch (IllegalArgumentException e) {
            errorHandler.log(e);
            return MessageType.OTHER;
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
