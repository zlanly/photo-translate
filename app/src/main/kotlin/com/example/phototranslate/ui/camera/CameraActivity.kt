package com.example.phototranslate.ui.camera

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.phototranslate.R
import com.example.phototranslate.databinding.ActivityCameraBinding
import com.example.phototranslate.domain.OcrResult
import com.example.phototranslate.domain.TextBlock
import com.example.phototranslate.usecase.DefaultOcrUseCase
import com.example.phototranslate.usecase.OcrUseCase
import com.example.phototranslate.ui.permission.PermissionUtils
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Camera Activity with Phase 4 UX improvements:
 * - Permission handling with fallback
 * - Loading state indicators
 * - Error messaging
 * - Dark theme support (via Material DayNight)
 */
class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var cameraExecutor = Executors.newFixedThreadPool(4)
    private var cameraXImageAnalysis: ImageAnalysis? = null

    private enum class Mode { LIVE, PHOTO }
    private var currentMode = Mode.LIVE

    private var lastOcrTime = 0L
    private val ocrThrottleInterval = 300L
    private val previousText = AtomicReference<String>("")
    private val textChangeThreshold = 5

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupModeToggle()
        setupCaptureButton()
        setupPermissionLaunchers()

        if (PermissionUtils.hasAllPermissions(this)) {
            startCamera()
        } else {
            requestPermissions()
        }
    }

    private fun setupModeToggle() {
        binding.modeToggleGroup.addOnButtonCheckedListener { _, checkedId, checked ->
            if (checked) when (checkedId) {
                R.id.btnRealtimeMode -> { currentMode = Mode.LIVE; setupLiveMode() }
                R.id.btnCaptureMode -> { currentMode = Mode.PHOTO; setupPhotoMode() }
            }
        }
        binding.btnRealtimeMode.setChecked(true)
        setupLiveMode()
    }

    private fun setupLiveMode() {
        binding.captureButton.visibility = android.view.View.GONE
        binding.textOverlay.visibility = android.view.View.VISIBLE
        binding.statusBar.visibility = android.view.View.VISIBLE
        updateStatusText("Live mode - Scanning...")
    }

    private fun setupPhotoMode() {
        binding.textOverlay.visibility = android.view.View.GONE
        binding.captureButton.visibility = android.view.VISIBLE
        binding.statusBar.visibility = android.view.View.GONE
    }

    private fun setupCaptureButton() {
        binding.captureButton.setOnClickListener {
            if (currentMode == Mode.PHOTO) takePhoto()
        }
    }

    private fun setupPermissionLaunchers() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) startCamera()
            else showPermissionDeniedToast()
        }
    }

    private fun requestPermissions() {
        if (!PermissionUtils.hasPermission(this, Manifest.permission.CAMERA)) {
            PermissionUtils.requestCameraPermission(this, {
                if (PermissionUtils.hasPermission(this, Manifest.permission.CAMERA)) startCamera()
                else showPermissionDeniedToast()
            })
        } else {
            startCamera()
        }
    }

    private fun showPermissionDeniedToast() {
        Toast.makeText(this, "Camera permission required to use this app", Toast.LENGTH_LONG).show()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)

                cameraXImageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(Size(1280, 720))
                    .build()
                    .also { it.setAnalyzer(cameraExecutor) { imageProxy -> processFrame(imageProxy) } }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, cameraXImageAnalysis!!)
                Log.d("CameraActivity", "CameraX started")
            } catch (exc: Exception) {
                Log.e("CameraActivity", "Camera binding failed", exc)
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processFrame(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastOcrTime < ocrThrottleInterval) {
            imageProxy.close()
            return
        }
        lastOcrTime = currentTime

        showLoading(true)
        updateStatusText("OCR detecting...")

        cameraExecutor.execute {
            val ocrResult = getOcrUseCase().analyze(imageProxy)
            Handler(Looper.getMainLooper()).post {
                showLoading(false)
                handleOcrResult(ocrResult, imageProxy)
            }
        }
    }

    private fun handleOcrResult(ocrResult: OcrResult, imageProxy: ImageProxy) {
        imageProxy.close()
        if (ocrResult.success && ocrResult.textRecognitionResult != null) {
            val fullText = ocrResult.textRecognitionResult.fullText
            binding.originalText.text = if (fullText.length > 100) fullText.substring(0, 100) + "..." else fullText

            if (currentMode == Mode.LIVE) checkAndTranslateText(fullText, emptyList())
        } else {
            binding.originalText.text = ""
            binding.translatedText.text = "No text detected - try another angle"
            updateStatusText("No text found")
        }
    }

    private fun checkAndTranslateText(newText: String, textBlocks: List<TextBlock>) {
        val previous = previousText.get()
        val newChars = newText.length - previous.length
        if (newChars >= textChangeThreshold || newText.isNotBlank() && previous.isEmpty()) {
            translateText(newText, "auto")
            previousText.set(newText)
        }
    }

    private fun translateText(text: String, sourceLanguage: String) {
        updateStatusText("Translating...")
        lifecycleScope.launch {
            delay(800) // Simulate network delay
            binding.translatedText.text = text.reversed() // Simulated
            updateStatusText("Live mode - Ready")
        }
    }

    private fun getOcrUseCase(): OcrUseCase {
        return DefaultOcrUseCase()
    }

    private fun takePhoto() {
        val intent = Intent(this, com.example.phototranslate.ui.result.ResultActivity::class.java).apply {
            putExtra("original_text", "Sample captured text")
            putExtra("translated_text", "Sample translated text")
            putExtra("source_lang", "en")
            putExtra("target_lang", "es")
        }
        startActivity(intent)
        finish()
    }

    private fun showLoading(show: Boolean) {
        binding.loadingOverlay.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun updateStatusText(text: String) {
        binding.statusText.text = text
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
