package com.example.phototranslate.domain

/**
 * Data class representing detected text from OCR.
 * Contains the raw text and its bounding box location.
 */
data class TextBlock(
    val text: String,
    val boundingBox: Rect,
    val language: String? = null,
    val confidence: Float = 1.0f
)

/**
 * Data class representing a complete recognition result.
 * Contains all text blocks from a single image.
 */
data class TextRecognitionResult(
    val blocks: List<TextBlock>,
    val fullText: String,
    val dominantLanguage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data class representing translation result.
 */
data class TranslationResult(
    val originalLanguage: String,
    val targetLanguage: String,
    val originalText: String,
    val translatedText: String,
    val errorMessage: String? = null,
    val confidence: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data class representing OCR operation result.
 * Used for error handling and status propagation.
 */
data class OcrResult(
    val success: Boolean,
    val textRecognitionResult: TextRecognitionResult? = null,
    val errorMessage: String? = null,
    val exception: Exception? = null
)

/**
 * Data class representing translation operation result.
 */
data class TranslationResultData(
    val success: Boolean,
    val translationResult: TranslationResult? = null,
    val errorMessage: String? = null,
    val exception: Exception? = null
)

/**
 * Helper class for rectangle coordinates (for bounding boxes).
 */
data class Rect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int)
