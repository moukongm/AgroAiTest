package com.common.storage.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DetectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DetectionRecord record);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<DetectionRecord> records);

    // 查询所有记录（按识别时间倒序）
    @Query("SELECT * FROM detection_records ORDER BY recognitionTime DESC")
    List<DetectionRecord> getAllRecords();


    // 清空表
    @Query("DELETE FROM detection_records")
    void deleteAll();

    @Delete
    void delete(DetectionRecord record);

    @Query("SELECT COUNT(*) FROM detection_records")
    int getCount();
}
