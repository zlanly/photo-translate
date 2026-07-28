package com.example.phototranslate.repository

import com.example.phototranslate.data.history.TranslationHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for translation history operations.
 * Abstracts the Room database details from the use case layer.
 */
interface HistoryRepository {

    /**
     * Save a new translation entry to history.
     */
    suspend fun saveEntry(original: String, translated: String, source: String, target: String, thumbnail: ByteArray? = null)

    /**
     * Get all history entries (most recent first).
     */
    fun getAllEntries(): Flow<List<TranslationHistoryEntity>>

    /**
     * Get history entries for a specific search term.
     */
    fun searchEntries(query: String): Flow<List<TranslationHistoryEntity>>

    /**
     * Delete a specific entry.
     */
    fun deleteEntry(entry: TranslationHistoryEntity)

    /**
     * Delete all history entries.
     */
    fun deleteAll()

    /**
     * Mark an entry as favorite/unfavorite.
     */
    fun toggleFavorite(entry: TranslationHistoryEntity)

    /**
     * Get all favorited entries.
     */
    fun getFavorites(): Flow<List<TranslationHistoryEntity>>

    /**
     * Get the total count of history entries.
     */
    fun getCount(): Flow<Int>
}
