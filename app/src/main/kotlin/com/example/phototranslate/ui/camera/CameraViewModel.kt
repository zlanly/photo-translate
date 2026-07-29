package com.example.phototranslate.ui.camera

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.phototranslate.domain.OcrResult
import com.example.phototranslate.domain.TextRecognitionResult
import com.example.phototranslate.domain.TranslationResult
import com.example.phototranslate.usecase.DefaultOcrUseCase
import com.example.phototranslate.usecase.DefaultTranslateUseCase
import com.example.phototranslate.usecase.OcrUseCase
import com.example.phototranslate.usecase.TranslateUseCase
import kotlinx.coroutines.launch

/**
 * ViewModel for CameraActivity.
 * Orchestrates the camera → OCR → translation pipeline in both Live and Photo modes.
 */
class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val _cameraStatus = MutableLiveData<String>()
    val cameraStatus: LiveData<String> = _cameraStatus

    private val _ocrResult = MutableLiveData<OcrResult>()
    val ocrResult: LiveData<OcrResult> = _ocrResult

    private val _translationResult = MutableLiveData<TranslationResult>()
    val translationResult: LiveData<TranslationResult> = _translationResult

    private val _uiMessage = MutableLiveData<String>()
    val uiMessage: LiveData<String> = _uiMessage

    private val _showLoading = MutableLiveData<Boolean>()
    val showLoading: LiveData<Boolean> = _showLoading

    // Use cases (injected via Hilt in production)
    private val ocrUseCase: OcrUseCase = DefaultOcrUseCase()
    private val translateUseCase: TranslateUseCase = DefaultTranslateUseCase()

    enum class Mode { LIVE, PHOTO }
    private var currentMode = Mode.LIVE

    init {
        _cameraStatus.value = "Ready"
    }

    fun setMode(mode: Mode) {
        currentMode = mode
        _cameraStatus.value = "Mode: ${if (mode == Mode.LIVE) "Live" else "Photo"}"
    }

    fun processFrame(imageProxy: androidx.camera.core.ImageProxy, currentTime: Long, lastOcrTime: java.util.concurrent.atomic.AtomicReference<Long>): Boolean {
        val throttleInterval = 300L
        if (currentTime - lastOcrTime.get() < throttleInterval) {
            imageProxy.close()
            return false
        }
        lastOcrTime.set(currentTime)
        _showLoading.value = true
        _uiMessage.value = "Detecting text..."

        viewModelScope.launch {
            val ocrResult = ocrUseCase.analyze(imageProxy)
            _showLoading.value = false
            _ocrResult.value = ocrResult

            if (ocrResult.success && ocrResult.textRecognitionResult != null) {
                val fullText = ocrResult.textRecognitionResult.fullText
                if (currentMode == Mode.LIVE) translateText(fullText)
                else _uiMessage.value = "Text detected: $fullText"
            } else {
                _uiMessage.value = "No text detected"
            }
            imageProxy.close()
        }

        return true
    }

    private fun translateText(text: String) {
        _uiMessage.value = "Translating..."
        viewModelScope.launch {
            try {
                val result = translateUseCase.translate(text, "auto", "en")
                _translationResult.value = result
                _uiMessage.value = "Translation complete"
            } catch (e: Exception) {
                _uiMessage.value = "Translation failed: ${e.message}"
            }
        }
    }

    fun capturePhoto(bitmap: Bitmap) {
        _showLoading.value = true
        _uiMessage.value = "Processing photo..."

        viewModelScope.launch {
            val ocrResult = ocrUseCase.analyze(bitmap)
            _ocrResult.value = ocrResult

            if (ocrResult.success && ocrResult.textRecognitionResult != null) {
                val text = ocrResult.textRecognitionResult.fullText
                val translation = translateUseCase.translate(text, "auto", "en")
                _translationResult.value = translation
                _uiMessage.value = "Photo processed"
            } else {
                _uiMessage.value = "OCR failed on photo"
            }
            _showLoading.value = false
        }
    }

    fun getCurrentMode(): Mode = currentMode
}
