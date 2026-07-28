package com.example.phototranslate.repository

import com.example.phototranslate.data.history.AppDatabase
import com.example.phototranslate.data.history.TranslationHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Default implementation of HistoryRepository using Room database.
 */
class DefaultHistoryRepository(private val database: AppDatabase) : HistoryRepository {

    private val dao = database.translationHistoryDao()

    override suspend fun saveEntry(original: String, translated: String, source: String, target: String, thumbnail: ByteArray?) {
        val entity = TranslationHistoryEntity(
            originalText = original,
            translatedText = translated,
            sourceLanguage = source,
            targetLanguage = target,
            thumbnail = thumbnail
        )
        dao.insert(entity)
    }

    override fun getAllEntries(): Flow<List<TranslationHistoryEntity>> = dao.getAllEntries()

    override fun searchEntries(query: String): Flow<List<TranslationHistoryEntity>> =
        dao.searchEntries("%$query%")

    override fun deleteEntry(entry: TranslationHistoryEntity) = dao.delete(entry)

    override fun deleteAll() = dao.deleteAll()

    override fun toggleFavorite(entry: TranslationHistoryEntity) {
        entry.isFavorite = !entry.isFavorite
        dao.update(entry)
    }

    override fun getFavorites(): Flow<List<TranslationHistoryEntity>> = dao.getFavorites()

    override fun getCount(): Flow<Int> = dao.getCount()
}
