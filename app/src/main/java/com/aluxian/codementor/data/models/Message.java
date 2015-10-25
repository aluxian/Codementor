package com.aluxian.codementor.data.models;

import android.support.annotation.NonNull;

import com.aluxian.codementor.data.types.MessageType;
import com.aluxian.codementor.data.utils.MessageParsers;

import java.util.Date;

public class Message implements Comparable<Message> {

    private MessageData messageData;
    private String loggedInUsername;

    private MessageType type;
    private long createdAt;
    private String typeContent;

    public Message(MessageData messageData, String loggedInUsername) {
        this.messageData = messageData;
        this.loggedInUsername = loggedInUsername;
    }

    public Message(FirebaseMessage firebaseMessage, String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
        this.messageData = new MessageData(
                firebaseMessage.getId(),
                firebaseMessage.getContent(),
                firebaseMessage.getSender(),
                firebaseMessage.getReceiver(),
                firebaseMessage.getRequest(),
                MessageParsers.DATE_FORMAT.format(new Date()),
                firebaseMessage.getReadAt(),
                firebaseMessage.getType()
        );
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
            createdAt = MessageParsers.parseDate(messageData.getCreatedAt());
        }

        return createdAt;
    }

    public MessageType getType() {
        if (type == null) {
            type = MessageParsers.parseType(messageData.getType());
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
            typeContent = MessageParsers.parseTypeContent(getType(), getContent(),
                    sentByCurrentUser(), getOtherUser(), getRequest());
        }

        return typeContent;
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

    @Override
    public int compareTo(@NonNull Message another) {
        if (equals(another)) {
            return 0;
        }

        if (getCreatedAt() < another.getCreatedAt()) {
            return 1;
        } else if (getCreatedAt() > another.getCreatedAt()) {
            return -1;
        } else {
            return 0;
        }
    }

}
