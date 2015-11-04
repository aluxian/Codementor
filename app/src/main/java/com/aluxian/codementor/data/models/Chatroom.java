package com.aluxian.codementor.data.models;

import com.aluxian.codementor.data.converters.MessageTypeConverter;
import com.aluxian.codementor.data.converters.StringIdTypeConverter;
import com.aluxian.codementor.data.types.MessageType;
import com.aluxian.codementor.utils.ContentComparable;
import com.aluxian.codementor.utils.Helpers;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;

import static com.aluxian.codementor.services.UserManager.LOGGED_IN_USERNAME;
import static com.aluxian.codementor.utils.Helpers.italic;

@JsonObject
public class Chatroom implements Serializable, ContentComparable<Chatroom> {

    @JsonField(name = "chatroom_id") String chatroomId;
    @JsonField(name = "chatroom_firebase_id") String chatroomFirebaseId;
    @JsonField(typeConverter = StringIdTypeConverter.class) long id;
    @JsonField(typeConverter = MessageTypeConverter.class) MessageType type;

    @JsonField String content;
    @JsonField Request request;

    @JsonField User sender;
    @JsonField User receiver;

    private String contentDescription;

    public Chatroom() {}

    public Chatroom(Chatroom chatroom) {
        this.chatroomId = chatroom.chatroomId;
        this.chatroomFirebaseId = chatroom.chatroomFirebaseId;
        this.id = chatroom.id;
        this.type = chatroom.type;
        this.content = chatroom.content;
        this.request = chatroom.request;
        this.sender = chatroom.sender;
        this.receiver = chatroom.receiver;
    }

    public long getId() {
        return id;
    }

    public String getChatroomId() {
        return chatroomId;
    }

    public String getFirebasePath() {
        return String.format("chatrooms/%s/%s", chatroomFirebaseId, chatroomId);
    }

    public User getCurrentUser() {
        return sender.getUsername().equals(LOGGED_IN_USERNAME) ? sender : receiver;
    }

    public User getOtherUser() {
        return sender.getUsername().equals(LOGGED_IN_USERNAME) ? receiver : sender;
    }

    public String getContentDescription() {
        if (contentDescription == null) {
            contentDescription = generateContentDescription();
        }

        return contentDescription;
    }

    public void setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }

    private String generateContentDescription() {
        boolean sentByMe = sender.equals(getCurrentUser());
        switch (type) {
            case MESSAGE:
                if (sentByMe) {
                    return italic("You: ") + content;
                }

                return content;

            case CONNECT:
                if (sentByMe) {
                    return italic("You requested a session.");
                }

                return italic(getOtherUser().getShortestName() + " requested a session.");

            case FILE:
                return italic(Helpers.escapeHtml(request.getFilename()));

            case REQUEST:
                if (sentByMe) {
                    return italic("You attached a request.");
                }

                return italic(getOtherUser().getShortestName() + " attached a request.");

            case SIGNATURE:
                if (sentByMe) {
                    return italic("You initiated an NDA request.");
                }

                return italic(getOtherUser().getShortestName() + " initiated an NDA request.");

            default:
                return italic("Unsupported message type.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chatroom)) return false;
        Chatroom chatroom = (Chatroom) o;
        return Objects.equal(id, chatroom.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean contentEquals(Chatroom another) {
        return Objects.equal(getContentDescription(), another.getContentDescription())
                && getOtherUser().contentEquals(another.getOtherUser());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("chatroomId", chatroomId)
                .add("content", getContentDescription())
                .toString();
    }

}
