package com.common.storage.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PostDao {
    // 插入或更新帖子
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PostEntity post);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<PostEntity> posts);

    // 查询所有帖子（按发布时间倒序）
    @Query("SELECT * FROM post_record ORDER BY publishTime DESC")
    List<PostEntity> getAllPosts();
    //
    @Query("UPDATE post_record SET userAvatarUrl = :newAvatarUrl ")
    void updateAvatar( String newAvatarUrl);

    //
    @Query("UPDATE post_record SET userName = :newUserName")
    void updateUserName( String newUserName);

}
