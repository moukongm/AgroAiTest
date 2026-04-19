package com.common.storage.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "post_record")
public class PostEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;                  // 本地自增ID
    private String userAvatarUrl;     // 发帖用户头像
    private String userName;          // 发帖用户名
    private String focusCrop;         // 关注的作物
    private String postImageUrls;     // 帖子图片URL列表（JSON数组）
    private String postDescription;   // 帖子描述内容
    private int favoriteCount;        // 收藏数量
    private long publishTime;         // 发布时间戳

    public PostEntity(int favoriteCount, String focusCrop, long id, String postDescription, String postImageUrls, long publishTime, String userAvatarUrl, String userName) {
        this.favoriteCount = favoriteCount;
        this.focusCrop = focusCrop;
        this.id = id;
        this.postDescription = postDescription;
        this.postImageUrls = postImageUrls;
        this.publishTime = publishTime;
        this.userAvatarUrl = userAvatarUrl;
        this.userName = userName;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public String getFocusCrop() {
        return focusCrop;
    }

    public void setFocusCrop(String focusCrop) {
        this.focusCrop = focusCrop;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public String getPostImageUrls() {
        return postImageUrls;
    }

    public void setPostImageUrls(String postImageUrls) {
        this.postImageUrls = postImageUrls;
    }

    public long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
