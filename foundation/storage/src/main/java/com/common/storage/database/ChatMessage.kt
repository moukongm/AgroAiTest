package com.common.storage.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AI 助手对话消息数据实体
 * 用于在本地数据库中存储与 AI 的对话记录
 */
@Entity(tableName = "chat_messages")
data class ChatMessage(
    /**
     * 唯一主键，自增ID
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * 会话 ID，用于区分不同的对话轮次
     */
    val sessionId: String,
    
    /**
     * 消息发送者的角色，通常为 "user"（用户） 或 "ai"（AI助手）
     */
    val role: String, // "user" or "ai"
    
    /**
     * 消息的具体文本内容
     */
    val content: String,
    
    /**
     * 消息产生的时间戳，默认为当前时间
     */
    val timestamp: Long = System.currentTimeMillis()
)
