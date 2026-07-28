package com.example.phototranslate.data.history

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * TranslationHistoryEntity - Room database entity for storing translation history.
 * Each entry includes the original text, translated text, language pair, timestamp,
 * and optional image thumbnail (as compressed byte array).
 */
@Entity(tableName = "translation_history")
data class TranslationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val originalText: String,
    val translatedText: String,

    val sourceLanguage: String,
    val targetLanguage: String,

    val timestamp: Long = System.currentTimeMillis(),

    // Optional thumbnail as compressed bitmap (limited size for storage efficiency)
    val thumbnail: ByteArray? = null,

    // Whether this entry was marked as favorite
    val isFavorite: Boolean = false
)
