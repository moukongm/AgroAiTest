package com.common.storage.database;


import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "crop")
public class CropRecord {
    @PrimaryKey(autoGenerate = true)
    private long id;               // 本地自增ID
    private long cropId;           // 服务器作物ID（用于删除等操作）
    private String cropName;       // 作物名称（如：水稻、小麦）
    private String cropImageUrl;   // 作物图片URL
    private String avatarLocalPath; //头像

    // 无参构造函数（Room 需要）
    public CropRecord() {
    }

    @Ignore
    // 带参构造函数（不包含 id，因为 id 由数据库自动生成）
    public CropRecord(String cropName, String cropImageUrl, String avatarLocalPath) {
        this.cropName = cropName;
        this.cropImageUrl = cropImageUrl;
        this.avatarLocalPath = avatarLocalPath;
    }

    @Ignore
    // 带 cropId 的构造函数
    public CropRecord(long cropId, String cropName, String cropImageUrl, String avatarLocalPath) {
        this.cropId = cropId;
        this.cropName = cropName;
        this.cropImageUrl = cropImageUrl;
        this.avatarLocalPath = avatarLocalPath;
    }

    // Getter 和 Setter
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getCropImageUrl() {
        return cropImageUrl;
    }

    public void setCropImageUrl(String cropImageUrl) {
        this.cropImageUrl = cropImageUrl;
    }

    public void setAvatarLocalPath(String avatarLocalPath) {
        this.avatarLocalPath = avatarLocalPath;
    }

    public String getAvatarLocalPath() {
        return avatarLocalPath;
    }

    public long getCropId() {
        return cropId;
    }

    public void setCropId(long cropId) {
        this.cropId = cropId;
    }

    @Override
    public String toString() {
        return "CropEntity{" +
                "id=" + id +
                ", cropId=" + cropId +
                ", cropName='" + cropName + '\'' +
                ", cropImageUrl='" + cropImageUrl + '\'' +
                ", avatarLocalPath='" + avatarLocalPath + '\'' +
                '}';
    }

}

