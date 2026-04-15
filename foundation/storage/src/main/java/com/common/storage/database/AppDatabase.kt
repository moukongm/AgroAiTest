package com.common.storage.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//实现database和实体类的统一绑定
@Database(entities = [RecognitionRecord::class, ChatMessage::class, SearchHistoryRecord::class, UserRecord::class, CropRecord::class,DetectionRecord::class, PostEntity::class], version = 2,exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recognitionDao(): RecognitionDao
    abstract fun chatDao(): ChatDao

    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun detectionDao(): DetectionDao

    abstract fun userDao(): UserDao

    abstract fun cropDao(): CropDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agroai_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
