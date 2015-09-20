package com.aluxian.codementor.models;

public class User {

    private String username;
    private String level;
    private float rating;
    private String name;
    private String first_name;
    private String role;
    private String avatar_url;
    private String small_avatar_url;

    public User() {}

    public String getUsername() {
        return username;
    }

    public String getLevel() {
        return level;
    }

    public float getRating() {
        return rating;
    }

    public String getName() {
        return name;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getRole() {
        return role;
    }

    public String getAvatarUrl() {
        return avatar_url;
    }

    public String getSmallAvatarUrl() {
        return small_avatar_url;
    }

    public String getShortestName() {
        return first_name != null ? first_name : name;
    }

}
