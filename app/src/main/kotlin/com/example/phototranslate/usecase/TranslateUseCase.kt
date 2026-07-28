package com.example.phototranslate.usecase

import android.graphics.Bitmap
import com.example.phototranslate.domain.ALL_LANGUAGE_OPTIONS
import com.example.phototranslate.domain.LanguageOption
import com.example.phototranslate.domain.ModelStatus
import com.example.phototranslate.domain.ModelDownloadEvent
import com.example.phototranslate.domain.ModelManagerResult
import com.google.mlkit.translate.TranslateLanguage
import com.google.mlkit.translate.TranslatorOptions
import kotlinx.coroutines.flow.Flow

/**
 * Use Case for performing translation operations.
 * Encapsulates ML Kit Translate logic including model management.
 */
interface TranslateUseCase : UseCase<TranslationResult> {
    /**
     * Translate text from source language to target language.
     * @param text The text to translate
     * @param sourceLanguageCode The source language code (use "auto" for detection)
     * @param targetLanguageCode The target language code
     * @return TranslationResult containing the translated text
     */
    fun translate(
        text: String,
        sourceLanguageCode: String,
        targetLanguageCode: String
    ): TranslationResult

    /**
     * Translate from a bitmap image - runs OCR then translation in one step.
     * Convenience method for quick photo-to-translation workflow.
     */
    fun translateBitmap(
        bitmap: Bitmap,
        sourceLanguageCode: String,
        targetLanguageCode: String
    ): TranslationResult

    /**
     * Check if a translation model is downloaded for the given language.
     */
    fun isModelDownloaded(languageCode: String): Boolean

    /**
     * Download the translation model for the given language.
     * Returns a Flow that emits progress updates (0.0f to 1.0f).
     */
    fun downloadModel(
        languageCode: String
    ): Flow<ModelManagerResult>

    /**
     * Delete a downloaded translation model.
     */
    fun deleteModel(languageCode: String): Flow<ModelManagerResult>

    /**
     * Get the current download status for all languages.
     */
    fun getModelStatuses(): Flow<List<ModelStatus>>

    /**
     * Observe model download progress events for a specific language.
     * Emits ModelDownloadEvent with progress updates.
     */
    fun observeDownloadProgress(
        languageCode: String
    ): Flow<ModelDownloadEvent>

    /**
     * Set the default target language for future translations.
     */
    fun setDefaultTargetLanguage(languageCode: String)

    /**
     * Get the default target language.
     */
    fun getDefaultTargetLanguage(): String?

    /**
     * Get all supported language options for the UI.
     */
    fun getSupportedLanguages(): List<LanguageOption>
}
