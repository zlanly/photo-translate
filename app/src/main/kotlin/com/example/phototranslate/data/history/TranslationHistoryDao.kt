package com.example.phototranslate.data.history

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * TranslationHistoryDAO - Data Access Object for translation history.
 * Provides query operations for the translation_history table.
 */
@Dao
interface TranslationHistoryDao {

    /**
     * Insert a new history entry.
     * If the entry already exists with the same id, it will be updated.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TranslationHistoryEntity)

    /**
     * Insert multiple history entries at once.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<TranslationHistoryEntity>)

    /**
     * Update an existing entry (e.g., mark as favorite).
     */
    @Update
    suspend fun update(entry: TranslationHistoryEntity)

    /**
     * Delete a specific entry by ID.
     */
    @Delete
    suspend fun delete(entry: TranslationHistoryEntity)

    /**
     * Delete all history entries.
     */
    @Query("DELETE FROM translation_history")
    suspend fun deleteAll()

    /**
     * Delete a specific entry by ID.
     */
    @Query("DELETE FROM translation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Get all history entries ordered by timestamp (most recent first).
     */
    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<TranslationHistoryEntity>>

    /**
     * Get a specific entry by ID.
     */
    @Query("SELECT * FROM translation_history WHERE id = :id")
    suspend fun getById(id: Long): TranslationHistoryEntity?

    /**
     * Search entries by original or translated text.
     */
    @Query("SELECT * FROM translation_history " +
           "WHERE originalText LIKE :searchTerm OR translatedText LIKE :searchTerm " +
           "ORDER BY timestamp DESC")
    fun searchEntries(searchTerm: String): Flow<List<TranslationHistoryEntity>>

    /**
     * Get count of all entries.
     */
    @Query("SELECT COUNT(*) FROM translation_history")
    fun getCount(): Flow<Int>

    /**
     * Get favorite entries only.
     */
    @Query("SELECT * FROM translation_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<TranslationHistoryEntity>>
}
