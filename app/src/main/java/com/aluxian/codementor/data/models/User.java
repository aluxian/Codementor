package com.aluxian.codementor.data.models;

import com.aluxian.codementor.data.types.PresenceType;
import com.aluxian.codementor.utils.Constants;
import com.aluxian.codementor.utils.ContentComparable;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class User implements Serializable, ContentComparable<User> {

    private PresenceType presenceType = PresenceType.OFFLINE;
    @JsonField(name = "first_name") @JsonProperty("first_name") String firstName;
    @JsonField(name = "avatar_url") @JsonProperty("avatar_url") String avatarUrl;
    @JsonField String username;
    @JsonField String name;

    public User() {}

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getShortestName() {
        return firstName != null ? firstName : name;
    }

    public String getChatroomUrl() {
        return String.format("%s/chatrooms/%s", Constants.SERVER_API_URL, username);
    }

    public String getReadUrl() {
        return String.format("%s/chatrooms/%s/read", Constants.SERVER_API_URL, username);
    }

    public String getPresencePath() {
        return String.format("presence/%s/magic", username);
    }

    public PresenceType getPresenceType() {
        return presenceType;
    }

    public void setPresenceType(PresenceType presenceType) {
        this.presenceType = presenceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equal(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }

    @Override
    public boolean contentEquals(User another) {
        return getPresenceType() == another.getPresenceType();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("username", getUsername())
                .toString();
    }

}
