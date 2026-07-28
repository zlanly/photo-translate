package com.example.phototranslate.domain

/**
 * Language option for translation.
 * Includes display name and ML Kit language code.
 */
data class LanguageOption(
    val code: String,
    val displayName: String,
    val flagEmoji: String = ""
)

/**
 * Supported language options - automatically populated from ML Kit.
 * "auto" is a special option for automatic source language detection.
 */
val ALL_LANGUAGE_OPTIONS: List<LanguageOption> = listOf(
    LanguageOption("auto", "Auto Detect", "🌍"),
    LanguageOption("en", "English", "🇺🇸"),
    LanguageOption("zh", "Chinese (Simplified)", "🇨🇳"),
    LanguageOption("ja", "Japanese", "🇯🇵"),
    LanguageOption("ko", "Korean", "🇰🇷"),
    LanguageOption("fr", "French", "🇫🇷"),
    LanguageOption("de", "German", "🇩🇪"),
    LanguageOption("es", "Spanish", "🇪🇸"),
    LanguageOption("ru", "Russian", "🇷🇺"),
    LanguageOption("pt", "Portuguese", "🇧🇷"),
    LanguageOption("ar", "Arabic", "🇸🇦"),
    LanguageOption("hi", "Hindi", "🇮🇳"),
    LanguageOption("it", "Italian", "🇮🇹"),
    LanguageOption("nl", "Dutch", "🇳🇱"),
    LanguageOption("tr", "Turkish", "🇹🇷"),
    LanguageOption("pl", "Polish", "🇵🇱"),
    LanguageOption("vi", "Vietnamese", "🇻🇳"),
    LanguageOption("th", "Thai", "🇹🇭"),
    LanguageOption("id", "Indonesian", "🇮🇩"),
)

/**
 * Model language download status.
 */
enum class ModelDownloadStatus {
    INSTALLED,    // Model already downloaded and available
    DOWNLOADING,  // Model is currently being downloaded
    NOT_INSTALLED // Model not downloaded, needs download,
    FAILED         // Download failed
}

/**
 * Data class representing model status for a language.
 */
data class ModelStatus(
    val languageCode: String,
    val downloadStatus: ModelDownloadStatus,
    val progress: Float = 0.0f, // 0.0f to 1.0f for DOWNLOADING state
    val errorMessage: String? = null
)

/**
 * Model download event for UI state updates.
 * Emits progress updates and final completion/failure.
 */
data class ModelDownloadEvent(
    val languageCode: String,
    val status: ModelDownloadStatus,
    val progress: Float,
    val errorMessage: String? = null
)

/**
 * Operation result for model management.
 */
data class ModelManagerResult(
    val success: Boolean,
    val modelStatus: ModelStatus? = null,
    val errorMessage: String? = null
)
