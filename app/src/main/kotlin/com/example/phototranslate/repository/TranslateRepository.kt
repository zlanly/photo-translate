package com.example.phototranslate.repository

import com.example.phototranslate.domain.ModelStatus
import com.example.phototranslate.domain.ModelDownloadEvent
import com.example.phototranslate.domain.ModelManagerResult
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for data access related to translation.
 * Encapsulates all logic for interacting with the ML Kit Translate engine,
 * including model management (download/delete) and actual translation.
 */
interface TranslateRepository {

    /**
     * Translate text from source to target language.
     * Handles model downloading automatically if needed.
     */
    fun translate(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): Result<String> // Returns either translated text or error

    /**
     * Check if translation model is installed for the given language.
     * @param languageCode The ML Kit language code (e.g., "en", "zh")
     * @return true if model is installed, false otherwise
     */
    fun isModelInstalled(languageCode: String): Boolean

    /**
     * Download the translation model for the specified language.
     * Returns a Flow that emits download progress updates.
     * @param languageCode The ML Kit language code
     * @return Flow emitting ModelManagerResult with progress updates
     */
    fun downloadModel(
        languageCode: String
    ): Flow<ModelManagerResult>

    /**
     * Delete a downloaded translation model.
     * @param languageCode The ML Kit language code
     * @return Flow emitting ModelManagerResult with deletion status
     */
    fun deleteModel(
        languageCode: String
    ): Flow<ModelManagerResult>

    /**
     * Get the download status of all available translation models.
     * @return Flow emitting list of ModelStatus for all languages
     */
    fun getModelStatuses(): Flow<List<ModelStatus>>

    /**
     * Observe model download progress events for a specific language.
     * Emits ModelDownloadEvent for each progress update and final completion/failure.
     * Useful for showing progress bars in UI.
     */
    fun observeDownloadProgress(
        languageCode: String
    ): Flow<ModelDownloadEvent>

    /**
     * Get supported languages from ML Kit.
     * Returns a list of language codes and display names.
     */
    fun getSupportedLanguages(): List<com.example.phototranslate.domain.LanguageOption>

    /**
     * Configure the translator with specific options.
     */
    fun configure(options: TranslatorOptions)

    /**
     * Clean up resources.
     */
    fun shutdown()
}
