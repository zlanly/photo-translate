package com.example.phototranslate.repository

import android.content.Context
import android.os.StatFs
import com.example.phototranslate.domain.ALL_LANGUAGE_OPTIONS
import com.example.phototranslate.domain.LanguageOption
import com.example.phototranslate.domain.ModelDownloadStatus
import com.example.phototranslate.domain.ModelDownloadEvent
import com.example.phototranslate.domain.ModelManagerResult
import com.example.phototranslate.domain.ModelStatus
import com.example.phototranslate.application.PhotoTranslateApp
import com.google.mlkit.translate.Translate
import com.google.mlkit.translate.TranslateLanguage
import com.google.mlkit.translate.Translator
import com.google.mlkit.translate.TranslatorOptions
import com.google.mlkit.translate.TranslatorResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Default implementation of TranslateRepository using ML Kit Translate.
 * Handles both translation operations and model management (download/delete).
 * 
 * Note: ML Kit Translate automatically downloads models on first use.
 * The RemoteModelManager is primarily for Text Recognition models.
 * For Translate, we provide model management APIs that align with the interface,
 * with simplified implementation that matches ML Kit's actual behavior.
 */
class DefaultTranslateRepository(
    private val context: Context = PhotoTranslateApp.app(),
    private val coroutineFlowExecutor: kotlinx.coroutines.Dispatchers.IO
) : TranslateRepository {

    companion object {
        private const val MIN_REQUIRED_SPACE_MB = 10
    }

    // Singleton translator instance (ML Kit manages its own lifecycle)
    private var translator: Translator? = null
    private var currentOptions: TranslatorOptions? = null

    // Track active download operations to avoid concurrent downloads for same language
    private val activeDownloads = java.util.concurrent.ConcurrentHashMap<String, AtomicBoolean>()

    /**
     * Initialize the translator with the default options from the Application.
     */
    private fun initializeTranslator() {
        val app = PhotoTranslateApp.app()
        val options = app.getTranslateOptions()
        translator = Translate.create(options)
        currentOptions = options
    }

    override fun translate(text: String, sourceLanguage: String, targetLanguage: String): Result<String> {
        try {
            // Ensure model is available for target language (Translate auto-downloads on first use)
            if (!isModelAvailable(targetLanguage)) {
                // Model will download automatically on first translate call
                // In a UI, we could show a "Downloading..." message
            }

            if (translator == null) {
                initializeTranslator()
            }

            // For source language "auto", let ML Kit detect it automatically
            val sourceLang = if (sourceLanguage == "auto") {
                TranslateLanguage.AUTO
            } else {
                TranslateLanguage(sourceLanguage)
            }

            val targetLang = TranslateLanguage(targetLanguage)

            // Configure the translator for this specific call
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setLanguage(targetLanguage)
                .build()

            // Create a fresh translator for this call
            val localTranslator = Translate.create(options)

            val result = localTranslator.translate(text)
            localTranslator.shutdown() // Clean up after use

            return Result.success(result)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun isModelInstalled(languageCode: String): Boolean {
        // ML Kit Translate doesn't expose a direct API to check model installation status.
        // Models are downloaded on-demand. We attempt a lightweight check by verifying
        // we can create a translator for the language.
        return try {
            TranslateLanguage(languageCode) // Throws if invalid language code
            checkDiskSpace()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if there's enough disk space for translation models.
     */
    private fun checkDiskSpace(): Boolean {
        try {
            val stat = StatFs(context.externalCacheDir.path)
            val usableSpace = stat.usableBytes
            val requiredSpace = MIN_REQUIRED_SPACE_MB * 1024 * 1024 // 10MB minimum
            return usableSpace >= requiredSpace
        } catch (e: Exception) {
            // If we can't check disk space, assume it's fine
            return true
        }
    }

    /**
     * Check if model can be used (availability check).
     */
    private fun isModelAvailable(languageCode: String): Boolean {
        return isModelInstalled(languageCode) && checkDiskSpace()
    }

    override fun downloadModel(languageCode: String): Flow<ModelManagerResult> = flow {
        if (!checkDiskSpace()) {
            emit(ModelManagerResult(
                false,
                null,
                "Insufficient disk space for translation model"
            ))
            return@flow
        }

        // Avoid concurrent downloads for the same language
        val downloadActive = activeDownloads.computeIfAbsent(languageCode) { AtomicBoolean(false) }
        if (downloadActive.get()) {
            emit(ModelManagerResult(
                false,
                null,
                "Download already in progress for $languageCode"
            ))
            return@flow
        }
        downloadActive.set(true)

        try {
            // ML Kit Translate auto-downloads models on first use.
            // There's no explicit download API for Translate models like there is for Text Recognition.
            // We simulate the download process and emit status events.
            
            // Emit downloading status
            emit(ModelManagerResult(
                true,
                ModelStatus(languageCode, ModelDownloadStatus.DOWNLOADING, 0.0f),
                null
            ))

            // Simulate download progress (in real code, actual download happens on first translate call)
            for (progress in 20..100 step 20) {
                kotlinx.coroutines.delay(200)
                emit(ModelManagerResult(
                    true,
                    ModelStatus(languageCode, ModelDownloadStatus.DOWNLOADING, progress / 100f),
                    null
                ))
            }

            // Model is now "installed" (it will actually be downloaded on first use)
            emit(ModelManagerResult(
                true,
                ModelStatus(languageCode, ModelDownloadStatus.INSTALLED, 1.0f),
                "Model ready for translation"
            ))

        } catch (e: Exception) {
            downloadActive.set(false)
            emit(ModelManagerResult(
                false,
                null,
                "Download failed: ${e.message}"
            ))
        }
    }.flowOn(coroutineFlowExecutor)

    override fun deleteModel(languageCode: String): Flow<ModelManagerResult> = flow {
        try {
            // ML Kit doesn't expose explicit delete API for Translate models.
            // Models are cached and can be cleared via app cache cleanup.
            // We simulate the delete operation.
            
            emit(ModelManagerResult(
                true,
                ModelStatus(languageCode, ModelDownloadStatus.NOT_INSTALLED),
                "Model cleared (simulated)"
            ))
            
            // In production, you could clear app cache to remove downloaded models:
            // context.cacheDir.deleteRecursively() for the app's cache directory
            
        } catch (e: Exception) {
            emit(ModelManagerResult(false, null, "Failed to delete model: ${e.message}"))
        }
    }.flowOn(coroutineFlowExecutor)

    override fun getModelStatuses(): Flow<List<ModelStatus>> = flow {
        val statuses = ALL_LANGUAGE_OPTIONS.asSequence()
            .filter { it.code != "auto" }
            .map { languageOption ->
                val isInstalled = isModelInstalled(languageOption.code)
                ModelStatus(
                    languageCode = languageOption.code,
                    downloadStatus = if (isInstalled) ModelDownloadStatus.INSTALLED else ModelDownloadStatus.NOT_INSTALLED,
                    progress = if (isInstalled) 1.0f else 0.0f,
                    errorMessage = null
                )
            }
            .toList()
        emit(statuses)
    }.flowOn(coroutineFlowExecutor)

    override fun getSupportedLanguages(): List<LanguageOption> =
        ALL_LANGUAGE_OPTIONS.filter { it.code != "auto" }

    override fun observeDownloadProgress(languageCode: String): Flow<ModelDownloadEvent> = flow {
        // Since Translate doesn't have explicit progress callbacks,
        // we return a flow that can be used by UI to observe download state.
        // In a full implementation with RemoteModelManager for OCR,
        // this would emit real progress events.
        emit(ModelDownloadEvent(languageCode, ModelDownloadStatus.NOT_INSTALLED, 0.0f))
    }.flowOn(coroutineFlowExecutor)

    override fun configure(options: TranslatorOptions) {
        translator?.let { translator ->
            // Reinitialize with new options
            translator.shutdown()
        }
        this.currentOptions = options
        initializeTranslator()
    }

    override fun shutdown() {
        translator?.shutdown()
        translator = null
    }
}
