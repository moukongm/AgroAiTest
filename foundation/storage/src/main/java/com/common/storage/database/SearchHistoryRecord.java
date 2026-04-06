package com.common.storage.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "search_history")
public class SearchHistoryRecord {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String keyword;

    private long timestamp;

    public SearchHistoryRecord() {
        this.keyword = "";
    }

    @Ignore
    public SearchHistoryRecord(@NonNull String keyword, long timestamp) {
        this.keyword = keyword;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull
    public String getKeyword() { return keyword; }
    public void setKeyword(@NonNull String keyword) { this.keyword = keyword; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}