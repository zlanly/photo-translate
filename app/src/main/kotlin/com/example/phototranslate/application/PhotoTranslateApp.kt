package com.example.phototranslate.application

import android.app.Application
import android.content.Context
import com.google.mlkit.translate.TranslateLanguage
import com.google.mlkit.translate.TranslateOptions
import com.google.mlkit.textrecognition.TextRecognition
import com.google.mlkit.textrecognition.TextRecognizerOptions

/**
 * Main Application class for Photo Translate App.
 * Initializes ML Kit components and provides singleton access.
 * Uses Hilt for dependency injection.
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

    // ===== ML Kit Translation Singleton =====
    private val translateOptions: TranslateOptions by lazy {
        buildTranslateOptions()
    }

    /**
     * Build TranslateOptions with all supported languages pre-configured.
     * Languages list from ML Kit: https://developers.google.com/ml-kit/language-support
     * We pre-register commonly used languages to improve download performance.
     */
    private fun buildTranslateOptions(): TranslateOptions {
        val builder = TranslateOptions.Builder()

        // All supported languages for ML Kit Offline Translation
        // Full list at: https://developers.google.com/ml-kit/language-support/translation
        val supportedLanguages = listOf(
            TranslateLanguage.CHINESE_SIMPLIFIED,      // zh
            TranslateLanguage.CHINESE_TRADITIONAL,    // zh-TW
            TranslateLanguage.ENGLISH,                // en
            TranslateLanguage.JAPANESE,               // ja
            TranslateLanguage.KOREAN,                 // ko
            TranslateLanguage.FRENCH,                 // fr
            TranslateLanguage.GERMAN,                 // de
            TranslateLanguage.SPANISH,                // es
            TranslateLanguage.RUSSIAN,                // ru
            TranslateLanguage.PORTUGUESE,             // pt
            TranslateLanguage.ARABIC,                 // ar
            TranslateLanguage.HINDI,                  // hi
            TranslateLanguage.TURKISH,                // tr
            TranslateLanguage.POLISH,                 // pl
            TranslateLanguage.ITALIAN,                // it
            TranslateLanguage.DUTCH,                  // nl
            TranslateLanguage.BENGALI,                // bn
            TranslateLanguage.THEI,                   // th
            TranslateLanguage.VIETNAMESE,             // vi
            TranslateLanguage.INDONESIAN,             // id
            TranslateLanguage.HEBREW,                 // he
            TranslateLanguage.FILIPINO,               // fil
            TranslateLanguage.TAMIL,                  // ta
            TranslateLanguage.MALAY,                  // ms
            TranslateLanguage.GREEK,                  // el
            TranslateLanguage.HUNGARIAN,              // hu
            TranslateLanguage.CZECH,                  // cs
            TranslateLanguage.DANISH,                 // da
            TranslateLanguage.NORWEGIAN,              // no
            TranslateLanguage.SWEDISH,                // sv
            TranslateLanguage.DUTCH,                  // nl
            TranslateLanguage.LATVIAN,                // lv
            TranslateLanguage.LITHUANUAN,             // lt
            TranslateLanguage.RUMANIAN,               // ro
            TranslateLanguage.SLOVAK,                 // sk
            TranslateLanguage.SLOVENIAN,              // sl
            TranslateLanguage.CROATIAN,               // hr
            TranslateLanguage.SERBIAN,                // sr
            TranslateLanguage.BULGARIAN,              // bg
            TranslateLanguage.BASQUE,                 // eu
            TranslateLanguage.BELARUSIAN,             // be
            TranslateLanguage.MAROCHI,                // mk
            TranslateLanguage.BRETON,                 // br
            TranslateLanguage.MALTESE,                // mt
            TranslateLanguage.ISLANDIC,               // is
            TranslateLanguage.WALSH,                  // cy
            TranslateLanguage.SCOTCH,                 // gd
            TranslateLanguage.WELSH,                  // cy
            TranslateLanguage.CATALAN,                // ca
            TranslateLanguage.PUNJABI,                // pa
            TranslateLanguage.MARATHI,                // mr
            TranslateLanguage.GUJARATI,               // gu
            TranslateLanguage.BENGALI,                // bn
            TranslateLanguage.ODIYA,                  // or
            TranslateLanguage.TELUGU,                 // te
            TranslateLanguage.KANNADA,                // kn
            TranslateLanguage.MALAYALAM,              // ml
            TranslateLanguage.TAMIL,                  // ta
            TranslateLanguage.BURMESE,                // my
            TranslateLanguage.NEPALI,                 // ne
            TranslateLanguage.MONGOLIAN,              // mn
            TranslateLanguage.HEALTHCARE,             // he
            TranslateLanguage.HEBREW                  // he
        )

        for (lang in supportedLanguages) {
            builder.addSupportedLanguage(lang)
        }

        // Enable offline support (download models when needed)
        // This allows translation without internet connection after model download
        return builder.build()
    }

    // ===== ML Kit Text Recognition Singleton =====
    private val textRecognizer: TextRecognition by lazy {
        TextRecognition.getClient(
            TextRecognizerOptions.newBuilder()
                .setPerformanceMode(TextRecognizerOptions.PERFORMANCE_MODE_FAST) // Fastest for real-time
                .setLanguageDetectionEnabled(true) // Enable auto language detection
                .build()
        )
    }

    /**
     * Get the shared TextRecognition client.
     * Configured for real-time performance with language detection enabled.
     */
    fun getTextRecognitionClient(): TextRecognition = textRecognizer

    /**
     * Get the shared TranslateOptions.
     * Used by the TranslationRepository to initialize the translator.
     */
    fun getTranslateOptions(): TranslateOptions = translateOptions
}
