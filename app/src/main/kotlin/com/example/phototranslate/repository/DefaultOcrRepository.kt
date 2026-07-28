package com.example.phototranslate.repository

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.example.phototranslate.domain.OcrResult
import com.example.phototranslate.domain.TextBlock
import com.example.phototranslate.domain.TextRecognitionResult
import com.example.phototranslate.application.PhotoTranslateApp
import com.google.mlkit.common.ModulePendingException
import com.google.mlkit.textdetect.TextBlock
import com.google.mlkit.textdetect.TextDetectorException
import com.google.mlkit.textrecognition.TextRecognition
import com.google.mlkit.textrecognition.TextRecognitionOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

/**
 * Default implementation of OcrRepository using ML Kit Text Recognition.
 * 
 * This repository handles all communication with the ML Kit Text Recognition engine.
 * It decouples the business logic (UseCase) from the ML Kit SDK details.
 */
class DefaultOcrRepository(
    private val textRecognitionClient: TextRecognition,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : OcrRepository {

    override fun analyze(bitmap: Bitmap): OcrResult =
        withContext(ioDispatcher) {
            try {
                // ML Kit Text Recognition expects a com.google.android.gms.tasks.Task
                // We use the API that accepts an Image object from ML Kit
                // For Bitmap, we need to convert to ML Kit Image or use the simpler API
                val inputImage = createInputImage(bitmap)
                val task = textRecognitionClient.process(inputImage)
                val taskResult = task.await() // Blocking call in background thread

                val blocks = taskResult.blocks.map { block ->
                    createTextBlock(block)
                }

                val fullText = blocks.joinToString(" ") { it.text }
                OcrResult(
                    success = true,
                    textRecognitionResult = TextRecognitionResult(
                        blocks = blocks,
                        fullText = fullText
                    )
                )
            } catch (e: Exception) {
                handleOcrError(e)
            }
        }

    override fun analyze(imageProxy: ImageProxy): OcrResult =
        withContext(ioDispatcher) {
            try {
                // Convert ImageProxy to ML Kit Image format directly (more efficient)
                val inputImage = createInputImageFromProxy(imageProxy)
                val task = textRecognitionClient.process(inputImage)
                val taskResult = task.await()

                val blocks = taskResult.blocks.map { block ->
                    createTextBlock(block)
                }

                val fullText = blocks.joinToString(" ") { it.text }
                OcrResult(
                    success = true,
                    textRecognitionResult = TextRecognitionResult(
                        blocks = blocks,
                        fullText = fullText
                    )
                )
            } catch (e: Exception) {
                handleOcrError(e)
            } finally {
                imageProxy.close() // Always close the proxy after processing
            }
        }

    override fun identifyLanguage(text: String): String? =
        withContext(ioDispatcher) {
            try {
                // ML Kit can detect language from text when using TextRecognitionOptions
                // with language detection enabled. For now, we return null and let
                // the UseCase handle auto-detection if needed.
                // In a full implementation, we could call a separate language detection API.
                null
            } catch (e: Exception) {
                null
            }
        }

    override fun getConfig(): Map<String, Any> = mapOf(
        "model_type" => "ON_DEVICE",
        "performance_mode" => "FAST",
        "language_detection" => true,
        "version" => "16.0.0"
    )

    override fun shutdown() {
        // ML Kit client should be recreated as needed; no explicit shutdown required
    }

    /**
     * Convert Android Bitmap to ML Kit InputImage.
     * This is a simplified conversion - in production, use a helper that handles
     * different bitmap configurations and rotation properly.
     */
    private fun createInputImage(bitmap: Bitmap): com.google.mlkit.core.InputImage {
        return com.google.mlkit.core.InputImage.fromBitmap(bitmap)
    }

    /**
     * Convert ImageProxy from CameraX to ML Kit InputImage.
     * More efficient than converting through Bitmap since we skip the bitmap conversion step.
     */
    private fun createInputImageFromProxy(imageProxy: ImageProxy): com.google.mlkit.core.InputImage {
        return com.google.mlkit.core.InputImage.fromMediaImage(
            imageProxy.image!!,
            imageProxy.imageInfo.rotationDegrees
        )
    }

    /**
     * Create a TextBlock domain object from ML Kit TextBlock.
     */
    private fun createTextBlock(mlKitBlock: TextBlock): TextBlock {
        val boundingBox = mlKitBlock.boundingBox
        return TextBlock(
            text = mlKitBlock.text,
            boundingBox = Rect(
                left = boundingBox.left(),
                top = boundingBox.top(),
                right = boundingBox.right(),
                bottom = boundingBox.bottom()
            ),
            language = mlKitBlock.languageCode,
            confidence = mlKitBlock.confidence.toFloat()
        )
    }

    /**
     * Handle OCR errors by wrapping them in OrcResult with success = false.
     */
    private fun handleOcrError(exception: Exception): OcrResult {
        return when (exception) {
            is TextDetectorException -> OcrResult(
                success = false,
                errorMessage = "Text detection failed: ${exception.message}",
                exception = exception
            )
            is ModulePendingException -> OcrResult(
                success = false,
                errorMessage = "ML Kit model not downloaded. Please download the model first.",
                exception = exception
            )
            else -> OcrResult(
                success = false,
                errorMessage = "OCR error: ${exception.message}",
                exception = exception
            )
        }
    }
}
