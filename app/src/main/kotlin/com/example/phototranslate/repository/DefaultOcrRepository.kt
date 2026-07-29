package com.example.phototranslate.repository

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.example.phototranslate.domain.OcrResult
import com.example.phototranslate.domain.Rect
import com.example.phototranslate.domain.TextBlock
import com.example.phototranslate.domain.TextRecognitionResult
import com.example.phototranslate.application.PhotoTranslateApp
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text.TextBlock as MlKitTextBlock
import com.google.mlkit.vision.text.TextRecognizer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Default implementation of OcrRepository using ML Kit Text Recognition.
 *
 * This repository handles all communication with the ML Kit Text Recognition engine.
 * It decouples the business logic (UseCase) from the ML Kit SDK details.
 */
class DefaultOcrRepository(
    private val textRecognitionClient: TextRecognizer,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : OcrRepository {

    override fun analyze(bitmap: Bitmap): OcrResult {
        return try {
            // ML Kit Text Recognition accepts an InputImage built from a Bitmap.
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val taskResult = Tasks.await(textRecognitionClient.process(inputImage))

            val blocks = taskResult.textBlocks.map { block ->
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

    override fun analyze(imageProxy: ImageProxy): OcrResult {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return OcrResult(success = false, errorMessage = "ImageProxy.image is null")
        }
        return try {
            // Convert ImageProxy to ML Kit InputImage format directly (more efficient).
            val inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )
            val taskResult = Tasks.await(textRecognitionClient.process(inputImage))

            val blocks = taskResult.textBlocks.map { block ->
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

    override fun identifyLanguage(text: String): String? {
        // ML Kit can detect language from text when using TextRecognizerOptions
        // with language detection enabled. For now, we return null and let
        // the UseCase handle auto-detection if needed.
        return null
    }

    override fun getConfig(): Map<String, Any> = mapOf(
        "model_type" to "ON_DEVICE",
        "performance_mode" to "FAST",
        "language_detection" to true,
        "version" to "16.0.0"
    )

    override fun shutdown() {
        // ML Kit client should be recreated as needed; no explicit shutdown required
    }

    /**
     * Create a TextBlock domain object from an ML Kit TextBlock.
     */
    private fun createTextBlock(mlKitBlock: MlKitTextBlock): TextBlock {
        val boundingBox = mlKitBlock.boundingBox
        return TextBlock(
            text = mlKitBlock.text,
            boundingBox = Rect(
                left = boundingBox?.left ?: 0,
                top = boundingBox?.top ?: 0,
                right = boundingBox?.right ?: 0,
                bottom = boundingBox?.bottom ?: 0
            ),
            language = mlKitBlock.recognizedLanguage,
            confidence = 1.0f
        )
    }

    /**
     * Handle OCR errors by wrapping them in OcrResult with success = false.
     */
    private fun handleOcrError(exception: Exception): OcrResult {
        return OcrResult(
            success = false,
            errorMessage = "OCR error: ${exception.message}",
            exception = exception
        )
    }
}
