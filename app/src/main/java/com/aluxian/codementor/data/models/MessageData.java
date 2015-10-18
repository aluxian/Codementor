package com.aluxian.codementor.data.models;

import com.aluxian.codementor.data.annotations.GsonModel;

@GsonModel
public class MessageData {

    public final String id;
    public final String content;
    public final User sender;
    public final User receiver;
    public final Request request;
    public final String created_at;
    public final String read_at;
    public final String type;

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
