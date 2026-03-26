package com.network.model.user;

import com.google.gson.annotations.SerializedName;

public class UserUpdate {

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
    public UserUpdate(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public UserUpdate(String avatarUrl, String fullName) {
        this.avatarUrl = avatarUrl;
        this.fullName = fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
