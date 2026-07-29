package com.example.phototranslate.application

import android.app.Application
import android.content.Context
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * Main Application class for Photo Translate App.
 * Initializes ML Kit components and provides singleton access.
 */
class PhotoTranslateApp : Application() {

    companion object {
        private var instance: PhotoTranslateApp? = null

        fun context(): Context = instance!!
        fun app(): PhotoTranslateApp = instance!!

        @JvmStatic
        @Synchronized
        fun onCreate(app: PhotoTranslateApp) {
            instance = app
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize the singleton instance so app()/context() are available to the
        // manually-provisioned repositories and use cases.
        instance = this
    }

    // ===== ML Kit Translation Singleton =====
    private val defaultTranslateOptions: TranslatorOptions by lazy {
        buildTranslateOptions()
    }

    /**
     * Build TranslatorOptions with a default source/target pair.
     * ML Kit Translate auto-downloads the required models on first use, so a single
     * default configuration is sufficient; per-call options are built in the repository.
     */
    private fun buildTranslateOptions(): TranslatorOptions {
        return TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.CHINESE)
            .build()
    }

    // ===== ML Kit Text Recognition Singleton =====
    private val textRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Get the shared TextRecognizer client.
     * Configured for real-time performance.
     */
    fun getTextRecognitionClient(): TextRecognizer = textRecognizer

    /**
     * Get the shared TranslatorOptions.
     * Used by the TranslationRepository to initialize the translator.
     */
    fun getTranslateOptions(): TranslatorOptions = defaultTranslateOptions
}
