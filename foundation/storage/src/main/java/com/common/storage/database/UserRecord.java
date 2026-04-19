package com.common.storage.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class UserRecord {
    @PrimaryKey(autoGenerate = true)
    private Long userId;
    private String avatarUrl;
    private String avatarLocalPath;
    private String location;
    private int followCount;       // 关注数
    private int fansCount;         // 粉丝数
    private int postCount;         // 帖子数
    private long lastUpdateTime;   // 最后同步时间戳

    // Room 需要无参构造函数（可设为 private，但最好 public）
    public UserRecord() {
    }

    public UserRecord(long userId, String avatarURL, String location, int followCount, int fansCount, int postCount, long lastUpdateTime) {
        this.userId = userId;
        this.avatarUrl = avatarURL;
        this.location = location;
        this.followCount = followCount;
        this.fansCount = fansCount;
        this.postCount = postCount;
        this.lastUpdateTime = lastUpdateTime;
    }

    // Getter and Setter
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarURL) {
        this.avatarUrl = avatarURL;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getFollowCount() {
        return followCount;
    }

    public void setFollowCount(int followCount) {
        this.followCount = followCount;
    }

    public int getFansCount() {
        return fansCount;
    }

    public void setFansCount(int fansCount) {
        this.fansCount = fansCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public String getAvatarLocalPath() {
        return avatarLocalPath;
    }

    public void setAvatarLocalPath(String avatarLocalPath) {
        this.avatarLocalPath = avatarLocalPath;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }


    @Override
    public String toString() {
        return "UserRecord{" +
                "userId='" + userId + '\'' +
                ", avatarURL='" + avatarUrl + '\'' +
                ", location='" + location + '\'' +
                ", followCount=" + followCount +
                ", fansCount=" + fansCount +
                ", postCount=" + postCount +
                ", lastUpdateTime=" + lastUpdateTime +
                '}';
    }
}