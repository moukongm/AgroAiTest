package com.common.storage.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecognitionDao {
    @Insert
    fun insert(record: RecognitionRecord): Long

    @Query("SELECT * FROM recognition_records ORDER BY timestamp DESC")
    fun getAllRecords(): List<RecognitionRecord>

    @Delete
    fun delete(record: RecognitionRecord)
}
