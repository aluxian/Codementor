package com.aluxian.codementor.data.models;

import android.support.annotation.NonNull;

import com.aluxian.codementor.data.types.PresenceType;
import com.aluxian.codementor.utils.Constants;
import com.aluxian.codementor.utils.ContentComparable;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import java.io.Serializable;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable, ContentComparable<User> {

    private @JsonIgnore PresenceType presenceType;
    private String first_name;
    private String avatar_url;
    private String username;
    private String name;

    public User() {
        presenceType = PresenceType.OFFLINE;
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

    public String getChatroomPath() {
        return Constants.chatroomUrl(getUsername());
    }

    public String getReadPath() {
        return Constants.chatroomReadUrl(getUsername());
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
    public int compareTo(@NonNull User another) {
        return ComparisonChain.start().compare(getName(), another.getName()).result();
    }

    @Override
    public boolean compareContentTo(User another) {
        return getPresenceType() == another.getPresenceType();
    }

}
