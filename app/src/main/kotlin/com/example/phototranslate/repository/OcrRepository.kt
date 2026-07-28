package com.example.phototranslate.repository

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.example.phototranslate.domain.OcrResult
import com.example.phototranslate.domain.TextRecognitionResult

/**
 * Repository interface for data access related to OCR.
 * Encapsulates all logic for interacting with the ML Kit Text Recognition engine.
 * This allows the UseCase layer to remain agnostic of data source details.
 */
interface OcrRepository {

    /**
     * Analyze text from a bitmap image.
     * Returns the recognized text blocks.
     * Throws exceptions on failure (wrapped in OcrResult).
     */
    fun analyze(bitmap: Bitmap): OcrResult

    /**
     * Analyze text from ImageProxy (CameraX format).
     * More efficient for real-time processing as it avoids bitmap conversion.
     */
    fun analyze(imageProxy: ImageProxy): OcrResult

    /**
     * Identify the language of the given text.
     * Useful when source language is set to "auto".
     * Returns the detected language code.
     */
    fun identifyLanguage(text: String): String?

    /**
     * Get the current ML Kit text recognition client configuration.
     */
    fun getConfig(): Map<String, Any>

    /**
     * Clean up resources and shut down the recognizer.
     */
    fun shutdown()
}
