package com.common.storage.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "detection_records")
public class DetectionRecord {
    //有住建值，自己生成；

    @PrimaryKey(autoGenerate = true)
    private long id;                  // 本地自增ID
    private String imageUrl;
    private String diseaseName;       // 病害名称（冗余字段）
//    private Float confidence;
//    private String treatment;
    private long recognitionTime;     // 识别时间戳（用于按日期排序）

    public DetectionRecord(String diseaseName, long id, String imageUrl, long recognitionTime) {
        this.diseaseName = diseaseName;
        this.id = id;
        this.imageUrl = imageUrl;
        this.recognitionTime = recognitionTime;
    }

    //    public Float getConfidence() {
//        return confidence;
//    }
//
//    public void setConfidence(Float confidence) {
//        this.confidence = confidence;
//    }


    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public long getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setId(long id) {
        this.id = id;
    }

//    public String getImageUrl() {
//        return imageUrl;
//    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getRecognitionTime() {
        return recognitionTime;
    }

    public void setRecognitionTime(long recognitionTime) {
        this.recognitionTime = recognitionTime;
    }

//    public String getTreatment() {
//        return treatment;
//    }
//
//    public void setTreatment(String treatment) {
//        this.treatment = treatment;
//    }
}
