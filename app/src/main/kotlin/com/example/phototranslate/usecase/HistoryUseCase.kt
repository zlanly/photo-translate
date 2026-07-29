package com.example.phototranslate.usecase

import com.example.phototranslate.data.history.TranslationHistoryEntity
import com.example.phototranslate.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use Case for history operations.
 */
interface HistoryUseCase {

    /**
     * Save a new translation to history.
     */
    suspend fun saveHistory(original: String, translated: String, source: String, target: String, thumbnail: ByteArray? = null)

    /**
     * Get all history entries.
     */
    fun getHistory(): Flow<List<TranslationHistoryEntity>>

    /**
     * Search history entries.
     */
    fun searchHistory(query: String): Flow<List<TranslationHistoryEntity>>

    /**
     * Delete an entry from history.
     */
    suspend fun deleteHistoryEntry(entry: TranslationHistoryEntity)

    /**
     * Delete all history.
     */
    suspend fun deleteAllHistory()

    /**
     * Toggle favorite status.
     */
    suspend fun toggleFavorite(entry: TranslationHistoryEntity)

    /**
     * Get favorited entries.
     */
    fun getFavorites(): Flow<List<TranslationHistoryEntity>>

    /**
     * Get history count.
     */
    fun getHistoryCount(): Flow<Int>
}
