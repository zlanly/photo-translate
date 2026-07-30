package com.example.phototranslate.application

import android.app.Application
import android.content.Context
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
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
    // 拉丁识别器：对英文/法文等拉丁字母文字最准。
    private val latinTextRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    // 中日韩识别器：覆盖中文/日文/韩文（也能读拉丁，但精度略低于拉丁识别器）。
    private val cjkTextRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    }

    /**
     * 拉丁字母识别器客户端。
     */
    fun getTextRecognitionClient(): TextRecognizer = latinTextRecognizer

    /**
     * 中日韩(CJK)识别器客户端。
     */
    fun getCjkTextRecognitionClient(): TextRecognizer = cjkTextRecognizer

    /**
     * Get the shared TranslatorOptions.
     * Used by the TranslationRepository to initialize the translator.
     */
    fun getTranslateOptions(): TranslatorOptions = defaultTranslateOptions
}
