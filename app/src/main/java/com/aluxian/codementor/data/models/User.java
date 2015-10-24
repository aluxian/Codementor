package com.aluxian.codementor.data.models;

import com.aluxian.codementor.Constants;
import com.aluxian.codementor.data.annotations.GsonModel;
import com.aluxian.codementor.data.annotations.JacksonModel;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@GsonModel
@JacksonModel
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {

    private String username;
    private String name;
    private String first_name;
    private String avatar_url;

    public User(String username, String name, String first_name, String avatar_url) {
        this.username = username;
        this.name = name;
        this.first_name = first_name;
        this.avatar_url = avatar_url;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatar_url;
    }

    public String getShortestName() {
        return first_name != null ? first_name : name;
    }

    public String getPresencePath() {
        return "presence/" + username + "/magic";
    }

    public String getChatroomPath() {
        return Constants.getApiUrl("chatrooms", getUsername());
    }

    public String getReadPath() {
        return Constants.getApiUrl("chatrooms", getUsername(), "read");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;
        return !(username != null ? !username.equals(user.username) : user.username != null);
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }

}
