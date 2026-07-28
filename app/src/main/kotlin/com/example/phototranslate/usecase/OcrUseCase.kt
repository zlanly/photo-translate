package com.example.phototranslate.usecase

import com.example.phototranslate.domain.OcrResult
import com.example.phototranslate.domain.TextRecognitionResult
import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.flow.Flow

/**
 * Use Case for performing OCR (Optical Character Recognition) operations.
 * Encapsulates ML Kit Text Recognition logic.
 */
interface OcrUseCase : UseCase<OcrResult> {
    /**
     * Perform OCR on a bitmap image.
     * @return OcrResult containing the recognition result or error
     */
    fun analyze(bitmap: Bitmap): OcrResult

    /**
     * Perform OCR on an ImageProxy from CameraX.
     * This is optimized for real-time use cases.
     */
    fun analyze(imageProxy: ImageProxy): OcrResult

    /**
     * Get the current text recognition status (optional for future extensions).
     */
    fun getStatus(): Flow<OcrStatus>
}

/**
 * OcrStatus enumeration for tracking the state of OCR operations.
 */
enum class OcrStatus {
    IDLE,       // Not processing
    PROCESSING, // Currently analyzing
    COMPLETED,  // Successfully completed
    ERROR       // Failed with error
}
