package com.example.phototranslate.usecase

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.camera.core.ImageProxy
import com.example.phototranslate.application.PhotoTranslateApp
import com.example.phototranslate.domain.ModelDownloadStatus
import com.example.phototranslate.domain.OcrResult
import com.example.phototranslate.domain.TranslationResult
import com.example.phototranslate.repository.DefaultOcrRepository
import com.example.phototranslate.repository.DefaultTranslateRepository
import com.example.phototranslate.repository.OcrRepository
import com.example.phototranslate.repository.TranslateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * DefaultOcrUseCase implementation.
 */
class DefaultOcrUseCase(
    private val ocrRepository: OcrRepository
) : OcrUseCase {

    constructor() : this(DefaultOcrRepository(PhotoTranslateApp.app().getTextRecognitionClient()))

    override fun analyze(imageProxy: ImageProxy): OcrResult = ocrRepository.analyze(imageProxy)
    override fun analyze(bitmap: Bitmap): OcrResult = ocrRepository.analyze(bitmap)
    override fun getStatus(): Flow<OcrStatus> = flow { emit(OcrStatus.IDLE) }
}

/**
 * DefaultTranslateUseCase with error handling and offline detection.
 */
class DefaultTranslateUseCase(
    private val translateRepository: TranslateRepository,
    private val ocrUseCase: OcrUseCase,
    private val context: Context = PhotoTranslateApp.app()
) : TranslateUseCase {

    constructor() : this(DefaultTranslateRepository(), DefaultOcrUseCase(), PhotoTranslateApp.app())

    private var defaultTargetLanguage: String? = "en"
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private fun hasInternet(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun isModelAvailable(languageCode: String): Boolean {
        val installed = translateRepository.isModelInstalled(languageCode)
        if (installed) return true
        if (!hasInternet()) return false
        return true
    }

    override fun translate(
        text: String,
        sourceLanguageCode: String,
        targetLanguageCode: String
    ): TranslationResult {
        if (text.isBlank() || text.length < 2) {
            return TranslationResult(sourceLanguageCode, targetLanguageCode, text, "", "Text too short")
        }

        if (!isModelAvailable(targetLanguageCode)) {
            return TranslationResult(sourceLanguageCode, targetLanguageCode, text, "", "Network unavailable")
        }

        val actualSourceLanguage = if (sourceLanguageCode == "auto") detectLanguage(text) else sourceLanguageCode
        return try {
            val result = translateRepository.translate(text, actualSourceLanguage, targetLanguageCode)
            result.map { TranslationResult(actualSourceLanguage, targetLanguageCode, text, it) }
                .getOrNull() ?: TranslationResult(actualSourceLanguage, targetLanguageCode, text, "", "Failed")
        } catch (e: Exception) {
            TranslationResult(actualSourceLanguage, targetLanguageCode, text, "", "Error: ${e.message}")
        }
    }

    override fun translateBitmap(
        bitmap: Bitmap,
        sourceLanguageCode: String,
        targetLanguageCode: String
    ): TranslationResult {
        val ocrResult = ocrUseCase.analyze(bitmap)
        if (!ocrResult.success) {
            return TranslationResult("unknown", targetLanguageCode, "", "", "OCR failed")
        }
        val text = (ocrResult.textRecognitionResult?.fullText ?: "")
        if (text.isEmpty()) {
            return TranslationResult(sourceLanguageCode, targetLanguageCode, "", "No text detected", "No text")
        }
        return translate(text, sourceLanguageCode, targetLanguageCode)
    }

    override fun isModelDownloaded(languageCode: String): Boolean =
        translateRepository.isModelInstalled(languageCode)

    override fun downloadModel(languageCode: String) = translateRepository.downloadModel(languageCode)
    override fun deleteModel(languageCode: String) = translateRepository.deleteModel(languageCode)
    override fun getModelStatuses() = translateRepository.getModelStatuses()
    override fun observeDownloadProgress(languageCode: String) = translateRepository.observeDownloadProgress(languageCode)
    override fun setDefaultTargetLanguage(languageCode: String) { defaultTargetLanguage = languageCode }
    override fun getDefaultTargetLanguage(): String? = defaultTargetLanguage
    override fun getSupportedLanguages() = translateRepository.getSupportedLanguages()

    private fun detectLanguage(text: String): String = "und"
}
