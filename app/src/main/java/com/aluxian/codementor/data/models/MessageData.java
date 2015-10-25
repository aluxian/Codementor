package com.aluxian.codementor.data.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageData {

    private String id;
    private String content;
    private User sender;
    private User receiver;
    private Request request;
    private String created_at;
    private String read_at;
    private String type;

    @SuppressWarnings("unused")
    public MessageData() {}

    public MessageData(String id, String content, User sender, User receiver, Request request, String created_at,
                       String read_at, String type) {
        this.id = id;
        this.content = content;
        this.sender = sender;
        this.receiver = receiver;
        this.request = request;
        this.created_at = created_at;
        this.read_at = read_at;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public Request getRequest() {
        return request;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public String getReadAt() {
        return read_at;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageData)) return false;

        MessageData that = (MessageData) o;
        return !(id != null ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
