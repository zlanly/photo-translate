package com.example.phototranslate.repository

import androidx.camera.core.ImageProxy
import com.example.phototranslate.domain.OcrResult
import com.google.mlkit.vision.text.TextRecognizer
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Regression tests for the camera OCR crash.
 *
 * Root cause of the crash: the analyzer ran [DefaultOcrRepository.analyze] (which closes the
 * ImageProxy in its `finally` block, owning the proxy lifecycle) and the caller then closed the
 * same ImageProxy again on the main thread, throwing IllegalStateException and crashing the app.
 *
 * These tests use only interface mocks ([ImageProxy], [TextRecognizer]) so they run on a plain JVM
 * without MockK's inline-mock agent. They assert the two invariants that prevent the crash:
 *   1. analyze never throws (even on a null image) — it returns a failed OcrResult instead.
 *   2. analyze closes the ImageProxy exactly once, so callers must never close it again.
 */
class DefaultOcrRepositoryTest {

    @Test
    fun analyze_nullImage_returnsFailure_andClosesProxyExactlyOnce() {
        val imageProxy = mockk<ImageProxy>()
        every { imageProxy.image } returns null

        var closeCount = 0
        every { imageProxy.close() } answers { closeCount++ }

        val recognizer = mockk<TextRecognizer>()
        val repo = DefaultOcrRepository(recognizer, recognizer)
        val result: OcrResult = repo.analyze(imageProxy)

        assertFalse("Null image must yield a failed (non-crashing) result", result.success)
        assertEquals("Proxy must be closed exactly once by the repository", 1, closeCount)
    }
}
