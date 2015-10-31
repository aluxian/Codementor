package com.aluxian.codementor.data.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageData {

    private String id;
    private String content;
    private Request request;

    private User sender;
    private User receiver;

    private String created_at;
    private String read_at;
    private String type;

    public MessageData() {}

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
        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
