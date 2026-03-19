package com.common.storage.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ChatDao {
    @Insert
    fun insert(message: ChatMessage): Long

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesBySession(sessionId: String): List<ChatMessage>

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    fun deleteSession(sessionId: String)
}
