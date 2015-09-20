package com.aluxian.codementor.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@SuppressWarnings("unused")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class User {

    private String timezone_str;
    private int timezone_offset;
    private String timezone_display;
    private String username;
    private String name;
    private String level;
    private float rating;
    private String first_name;
    private String role;
    private String avatar_url;
    private String small_avatar_url;

    public User() {}

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getAvatarUrl() {
        return avatar_url;
    }

    public String getShortestName() {
        return first_name != null ? first_name : name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        return username.equals(user.username);

    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

}
