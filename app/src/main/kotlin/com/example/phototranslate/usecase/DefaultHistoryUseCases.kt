package com.example.phototranslate.usecase

import com.example.phototranslate.data.history.TranslationHistoryEntity
import com.example.phototranslate.repository.DefaultHistoryRepository
import com.example.phototranslate.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow

/**
 * Default implementation of HistoryUseCase.
 */
class DefaultHistoryUseCase(private val historyRepository: HistoryRepository) : HistoryUseCase {

    constructor() : this(DefaultHistoryRepository(com.example.phototranslate.data.history.AppDatabase.getDatabase(com.example.phototranslate.application.PhotoTranslateApp.app())))

    override suspend fun saveHistory(original: String, translated: String, source: String, target: String, thumbnail: ByteArray?) {
        historyRepository.saveEntry(original, translated, source, target, thumbnail)
    }

    override fun getHistory(): Flow<List<TranslationHistoryEntity>> = historyRepository.getAllEntries()

    override fun searchHistory(query: String): Flow<List<TranslationHistoryEntity>> = historyRepository.searchEntries(query)

    override fun deleteHistoryEntry(entry: TranslationHistoryEntity) = historyRepository.deleteEntry(entry)

    override fun deleteAllHistory() = historyRepository.deleteAll()

    override fun toggleFavorite(entry: TranslationHistoryEntity) = historyRepository.toggleFavorite(entry)

    override fun getFavorites(): Flow<List<TranslationHistoryEntity>> = historyRepository.getFavorites()

    override fun getHistoryCount(): Flow<Int> = historyRepository.getCount()
}
