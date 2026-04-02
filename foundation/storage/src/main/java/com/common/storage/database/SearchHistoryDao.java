package com.common.storage.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface SearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SearchHistoryRecord record);

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC")
    Single<java.util.List<SearchHistoryRecord>> getAll();

    @Query("DELETE FROM search_history WHERE `keyword` = :keyword")
    void deleteByKeyword(String keyword);

    @Query("DELETE FROM search_history")
    void deleteAll();
}