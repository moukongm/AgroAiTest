package com.network.model.user;

import com.google.gson.annotations.SerializedName;

public class UserProfile {
    @SerializedName("id")
    private Long id;

    @SerializedName("username")
    private String username;

    @SerializedName("phone")
    private String phone;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    @SerializedName("bio")
    private String bio;

    @SerializedName("location")
    private String location;

    @SerializedName("followedCrops")
    private String followedCrops;

    public UserProfile(String avatarUrl, String followedCrops, String fullName, String phone) {
        this.avatarUrl = avatarUrl;
        this.followedCrops = followedCrops;
        this.fullName = fullName;
        this.phone = phone;
    }

    public UserProfile(String avatarUrl, String bio, String followedCrops, String fullName, Long id, String location, String phone, String username) {
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.followedCrops = followedCrops;
        this.fullName = fullName;
        this.id = id;
        this.location = location;
        this.phone = phone;
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getFollowedCrops() {
        return followedCrops;
    }

    public void setFollowedCrops(String followedCrops) {
        this.followedCrops = followedCrops;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
