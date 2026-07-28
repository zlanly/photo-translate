package com.example.phototranslate.data.history

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * AppDatabase - Main Room database for Photo Translate.
 * Contains the translation history table.
 */
@Database(
    entities = [TranslationHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun translationHistoryDao(): TranslationHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "photo_translate_database"
                )
                    .fallbackToDestructiveMigration() // Simplified for demo
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
