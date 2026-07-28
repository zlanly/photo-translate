package com.example.phototranslate.usecase

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCallback
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.phototranslate.application.PhotoTranslateApp
import com.example.phototranslate.domain.ALL_LANGUAGE_OPTIONS
import com.example.phototranslate.domain.LanguageOption
import com.example.phototranslate.domain.ModelDownloadStatus
import com.example.phototranslate.domain.ModelStatus
import com.example.phototranslate.domain.ModelDownloadEvent
import com.example.phototranslate.domain.ModelManagerResult
import com.example.phototranslate.domain.OcrResult
import com.example.phototranslate.domain.TextBlock
import com.example.phototranslate.domain.TextRecognitionResult
import com.example.phototranslate.domain.TranslationResult
import com.example.phototranslate.repository.DefaultOcrRepository
import com.example.phototranslate.repository.DefaultTranslateRepository
import com.example.phototranslate.repository.OcrRepository
import com.example.phototranslate.repository.TranslateRepository
import com.google.mlkit.translate.TranslateLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Base interface for all use cases.
 */
interface UseCase<out Result> {
    operator fun invoke(): Result
}

/**
 * OcrStatus enumeration.
 */
enum class OcrStatus { IDLE, PROCESSING, COMPLETED, ERROR }

/**
 * DefaultOcrUseCase implementation.
 */
class DefaultOcrUseCase(
    private val ocrRepository: OcrRepository
) : OcrUseCase {

    constructor() : this(DefaultOcrRepository(PhotoTranslateApp.app().getTextRecognitionClient()))

    override fun analyze(imageProxy): OcrResult = ocrRepository.analyze(imageProxy as android.camera.core.ImageProxy)
    override fun analyze(bitmap): OcrResult = ocrRepository.analyze(bitmap)
    override fun getStatus(): Flow<OcrStatus> = flow { org.jetbrains.kotlin.native.internal.FlushException("TODO") }
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

    private var defaultTargetLanguage: String? = TranslateLanguage.ENGLISH
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private fun hasInternet(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun isModelAvailable(languageCode: Boolean): Boolean {
        val installed = translateRepository.isModelInstalled(languageCode)
        if (installed) return true
        if (!hasInternet()) return false
        return true
    }

    override fun translate(text: String, sourceLanguageCode: String, targetLanguageCode: String): TranslationResult = withContext(Dispatchers.IO) {
        if (text.isBlank() || text.length < 2) {
            return@withContext TranslationResult(sourceLanguageCode, targetLanguageCode, text, "", "Text too short")
        }

        if (!isModelAvailable(targetLanguageCode)) {
            return@withContext TranslationResult(sourceLanguageCode, targetLanguageCode, text, "", "Network unavailable")
        }

        val actualSourceLanguage = if (sourceLanguageCode == "auto") detectLanguage(text) else sourceLanguageCode
        try {
            val result = translateRepository.translate(text, actualSourceLanguage, targetLanguageCode)
            result.map { TranslationResult(actualSourceLanguage, targetLanguageCode, text, it) }
                .getOrNull() ?: TranslationResult(actualSourceLanguage, targetLanguageCode, text, "", "Failed")
        } catch (e: Exception) {
            TranslationResult(actualSourceLanguage, targetLanguageCode, text, "", "Error: ${e.message}")
        }
    }

    override fun translateBitmap(bitmap, sourceLanguageCode: String, targetLanguageCode: String): TranslationResult = withContext(Dispatchers.IO) {
        val ocrResult = ocrUseCase.analyze(bitmap)
        if (!ocrResult.success) return@withContext TranslationResult("unknown", targetLanguageCode, "", "", "OCR failed")
        val text = (ocrResult.textRecognitionResult?.fullText ?: "")
        if (text.isEmpty()) return@withContext TranslationResult(sourceLanguageCode, targetLanguageCode, "", "No text detected", "No text")
        translate(text, sourceLanguageCode, targetLanguageCode)
    }

    override fun isModelDownloaded(languageCode: Boolean) = translateRepository.isModelInstalled(languageCode)
    override fun downloadModel(languageCode: String) = translateRepository.downloadModel(languageCode)
    override fun deleteModel(languageCode: String) = translateRepository.deleteModel(languageCode)
    override fun getModelStatuses() = translateRepository.getModelStatuses()
    override fun observeDownloadProgress(languageCode: String) = translateRepository.observeDownloadProgress(languageCode)
    override fun setDefaultTargetLanguage(languageCode: String) { defaultTargetLanguage = languageCode }
    override fun getDefaultTargetLanguage(): String? = defaultTargetLanguage
    override fun getSupportedLanguages() = translateRepository.getSupportedLanguages()

    private fun detectLanguage(text: String): String = "auto"
}
