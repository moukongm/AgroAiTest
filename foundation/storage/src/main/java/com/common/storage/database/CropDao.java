package com.common.storage.database;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
@Dao
public interface CropDao {
    // 插入作物（返回自增ID）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CropRecord crop);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<CropRecord> crops);
    // 更新作物
    @Update
    void update(CropRecord crop);
    // 删除作物
    @Delete
    void delete(CropRecord crop);
    // 根据ID删除（删除本地记录ID）
    @Query("DELETE FROM crop WHERE id = :cropId")
    void deleteById(long cropId);
    // 根据服务器 cropId 删除
    @Query("DELETE FROM crop WHERE cropId = :serverCropId")
    void deleteByCropId(long serverCropId);
    // 清空表
    @Query("DELETE FROM crop")
    void deleteAll();

    // 查询所有作物（按 id 升序）
    @Query("SELECT * FROM crop ORDER BY id ASC")
    List<CropRecord> getAllCrops();

    // 查询最新的最多3条作物（按 id 降序）
    @Query("SELECT * FROM crop ORDER BY id DESC LIMIT 3")
    List<CropRecord> getRecentCrops();
}
