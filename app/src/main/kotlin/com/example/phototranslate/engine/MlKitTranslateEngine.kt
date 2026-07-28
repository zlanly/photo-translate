package com.example.phototranslate.engine

import com.example.phototranslate.domain.TranslationResult
import com.example.phototranslate.usecase.TranslateUseCase

/**
 * MlKitTranslateEngine - Translation engine using ML Kit Translate.
 * Implements the TranslationEngine interface for pluggability.
 */
class MlKitTranslateEngine(private val translateUseCase: TranslateUseCase) : TranslationEngine {

    override fun translate(text: String, sourceLanguage: String, targetLanguage: String): TranslationResult {
        return translateUseCase.translate(text, sourceLanguage, targetLanguage)
    }

    override fun isAvailable(): Boolean = true // ML Kit is always available on device

    override fun getName(): String = "ML Kit (Offline)"

    override fun getSupportedLanguages(): List<String> {
        return translateUseCase.getSupportedLanguages().map { it.code }
    }

    override fun downloadModel(): Result<Unit> {
        try {
            // Models auto-download on first use in ML Kit
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun shutdown() {
        // No cleanup needed for ML Kit Translate
    }
}
