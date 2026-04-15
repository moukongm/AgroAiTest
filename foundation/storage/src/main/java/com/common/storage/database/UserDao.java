package com.common.storage.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


@Dao
public interface UserDao {
    // 插入或替换用户信息
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserRecord user);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UserRecord> users);
    // 更新用户
    @Update
    void update(UserRecord user);
    // 删除用户
    @Delete
    void delete(UserRecord user);
    // 根据ID查询
    @Query("SELECT * FROM user WHERE userId = :userId")
    UserRecord getUserById(long userId);
    // 查询所有用户
    @Query("SELECT * FROM user ORDER BY lastUpdateTime DESC")
    List<UserRecord> getAllUsers();
    // 清空表
    @Query("DELETE FROM user")
    void deleteAll();
}