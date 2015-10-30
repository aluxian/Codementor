package com.aluxian.codementor.data.models;

import android.support.annotation.NonNull;

import com.aluxian.codementor.data.types.MessageType;
import com.aluxian.codementor.utils.Constants;
import com.aluxian.codementor.utils.ContentComparable;
import com.aluxian.codementor.utils.Helpers;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import java.io.Serializable;

import static com.aluxian.codementor.utils.Helpers.italic;

public class Chatroom implements Serializable, ContentComparable<Chatroom> {

    private ChatroomData chatroomData;
    private String loggedInUsername;

    private long id;
    private MessageType lastMessageType;
    private String contentDescription;
    private long timestamp;

    public Chatroom(ChatroomData chatroomData, String loggedInUsername) {
        this.chatroomData = chatroomData;
        this.loggedInUsername = loggedInUsername;
    }

    public long getId() {
        if (id == 0) {
            id = Helpers.parseStringId(chatroomData.getId());
        }

        return id;
    }

    public String getChatroomId() {
        return chatroomData.getChatroomId();
    }

    public String getFirebasePath() {
        return Constants.chatroomPath(chatroomData.getChatroomFirebaseId(), chatroomData.getChatroomId());
    }

    public String getContentDescription() {
        if (contentDescription == null) {
            contentDescription = generateContentDescription();
        }

        return contentDescription;
    }

    public User getCurrentUser() {
        User sender = chatroomData.getSender();
        User receiver = chatroomData.getReceiver();

        if (sender.getUsername().equals(loggedInUsername)) {
            return sender;
        } else {
            return receiver;
        }
    }

    public User getOtherUser() {
        User sender = chatroomData.getSender();
        User receiver = chatroomData.getReceiver();

        if (!sender.getUsername().equals(loggedInUsername)) {
            return sender;
        } else {
            return receiver;
        }
    }

    public void updateContentDescription(Message message) {
        contentDescription = generateContentDescription(message);
    }

    private boolean sentByMe() {
        return chatroomData.getSender().equals(getCurrentUser());
    }

    private long getTimestamp() {
        if (timestamp == 0) {
            timestamp = Helpers.parseDate(chatroomData.getCreatedAt());
        }

        return timestamp;
    }

    private MessageType getLastMessageType() {
        if (lastMessageType == null) {
            lastMessageType = MessageType.parse(chatroomData.getType());
        }

        return lastMessageType;
    }

    private String generateContentDescription() {
        switch (getLastMessageType()) {
            case MESSAGE:
                if (sentByMe()) {
                    return italic("You: ") + chatroomData.getContent();
                }

                return chatroomData.getContent();

            case CONNECT:
                if (sentByMe()) {
                    return italic("You requested a session.");
                }

                return italic(getOtherUser().getShortestName() + " requested a session.");

            case FILE:
                return italic(Helpers.escapeHtml(chatroomData.getRequest().getFilename()));

            case REQUEST:
                if (sentByMe()) {
                    return italic("You attached a request.");
                }

                return italic(getOtherUser().getShortestName() + " attached a request.");

            case SIGNATURE:
                if (sentByMe()) {
                    return italic("You initiated a NDA request.");
                }

                return italic(getOtherUser().getShortestName() + " initiated a NDA request.");

            default:
                return italic("Unsupported message type.");
        }
    }

    private String generateContentDescription(Message message) {
        switch (message.getType()) {
            case MESSAGE:
                if (message.sentByMe()) {
                    return italic("You: ") + message.getRawContent();
                }

                return message.getRawContent();

            case CONNECT:
                if (message.sentByMe()) {
                    return italic("You requested a session.");
                }

                return italic(message.getOtherUser().getShortestName() + " requested a session.");

            case FILE:
                return italic(Helpers.escapeHtml(message.getRequest().getFilename()));

            case REQUEST:
                if (message.sentByMe()) {
                    return italic("You attached a request.");
                }

                return italic(message.getOtherUser().getShortestName() + " attached a request.");

            case SIGNATURE:
                if (message.sentByMe()) {
                    return italic("You initiated a NDA request.");
                }

                return italic(message.getOtherUser().getShortestName() + " initiated a NDA request.");

            default:
                return italic("Unsupported message type.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chatroom)) return false;
        Chatroom chatroom = (Chatroom) o;
        return Objects.equal(chatroomData, chatroom.chatroomData) &&
                Objects.equal(loggedInUsername, chatroom.loggedInUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(chatroomData, loggedInUsername);
    }

    @Override
    public int compareTo(@NonNull Chatroom another) {
        return -ComparisonChain.start().compare(getTimestamp(), another.getTimestamp()).result();
    }

    @Override
    public boolean compareContentTo(Chatroom another) {
        return Objects.equal(getContentDescription(), another.getContentDescription())
                && getOtherUser().compareContentTo(another.getOtherUser());
    }

}
