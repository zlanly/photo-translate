package com.example.phototranslate.engine

import com.example.phototranslate.domain.TranslationResult

/**
 * TranslationEngine - Pluggable translation backend interface.
 * Different engines can be swapped at runtime without changing the use case layer.
 * 
 * Examples of implementations:
 * - MlKitTranslateEngine (current default)
 * - PaddleOCREngine (Chinese OCR + translation)
 * - GeminiEngine (Google Gemini API)
 * - OfflineTranslateEngine (cached translations)
 */
interface TranslationEngine {

    /**
     * Translate text from source to target language.
     * @param text The text to translate
     * @param sourceLanguage Source language code (use "auto" for detection)
     * @param targetLanguage Target language code
     * @return TranslationResult with the result or error
     */
    fun translate(text: String, sourceLanguage: String, targetLanguage: String): TranslationResult

    /**
     * Check if the engine is available (e.g., model downloaded, API key configured).
     */
    fun isAvailable(): Boolean

    /**
     * Get the engine name/display name.
     */
    fun getName(): String

    /**
     * Get the engine's supported languages.
     */
    fun getSupportedLanguages(): List<String>

    /**
     * Download required models/assets for this engine.
     */
    fun downloadModel(): Result<Unit>

    /**
     * Clean up engine resources.
     */
    fun shutdown()
}
