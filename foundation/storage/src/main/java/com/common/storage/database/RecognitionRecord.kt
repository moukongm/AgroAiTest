package com.common.storage.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 识别记录数据实体
 * 用于在本地数据库中存储用户的病害识别历史记录
 */
@Entity(tableName = "recognition_records")
data class RecognitionRecord(
    /**
     * 唯一主键，自增ID
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * 识别图片的本地路径或网络URL
     */
    val imagePath: String,
    
    /**
     * 识别出的病害名称
     */
    val diseaseName: String,
    
    /**
     * 识别结果的置信度 (0.0 ~ 1.0)
     */
    val confidence: Float,
    
    /**
     * 建议的治疗方案或防治措施
     */
    val treatment: String,
    
    /**
     * 记录创建的时间戳，默认为当前时间
     */
    val timestamp: Long = System.currentTimeMillis()
)
