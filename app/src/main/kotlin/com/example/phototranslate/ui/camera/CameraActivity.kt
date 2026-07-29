package com.example.phototranslate.ui.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
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
import com.example.phototranslate.ui.history.HistoryActivity
import com.example.phototranslate.ui.language.LanguagePreferences
import com.example.phototranslate.ui.language.LanguageSelectActivity
import com.example.phototranslate.ui.result.ResultActivity
import com.example.phototranslate.ui.settings.SettingsActivity
import com.example.phototranslate.usecase.DefaultOcrUseCase
import com.example.phototranslate.usecase.DefaultTranslateUseCase
import com.example.phototranslate.usecase.OcrUseCase
import com.example.phototranslate.usecase.TranslateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * 相机页（启动页，全屏相机）。
 * - 顶部半透明栏：标题 + 语言 / 历史 / 设置入口
 * - 右上角「实时 / 拍照」分段切换（常驻可见）
 * - 实时模式：OCR → 真实 ML Kit 翻译，结果以圆角卡片浮于预览下方
 * - 拍照模式：真实拍照 → OCR → 翻译 → 打开结果页（不关闭相机，便于返回）
 */
class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private val cameraExecutor = Executors.newFixedThreadPool(4)
    private var imageCapture: ImageCapture? = null

    private val ocrUseCase: OcrUseCase = DefaultOcrUseCase()
    private val translateUseCase: TranslateUseCase = DefaultTranslateUseCase()

    private val mainHandler = Handler(Looper.getMainLooper())
    private var cameraStarted = AtomicBoolean(false)

    private enum class Mode { LIVE, PHOTO }
    private var currentMode = Mode.LIVE

    private var lastOcrTime = 0L
    private val ocrThrottleInterval = 400L
    private val previousText = AtomicReference("")

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPermissionLauncher()
        setupTopBar()
        setupModeToggle()
        setupCaptureButton()

        binding.btnRealtimeMode.isChecked = true
        setupLiveMode()

        checkPermissionAndStart()
    }

    override fun onResume() {
        super.onResume()
        // 从系统设置返回后，若已授权则启动相机
        if (!cameraStarted.get() && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        }
    }

    // ===== 权限 =====
    private fun setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) startCamera() else showPermissionDenied()
        }
    }

    private fun checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun showPermissionDenied() {
        binding.permissionDeniedOverlay.visibility = android.view.View.VISIBLE
        binding.btnRetryPermission.setOnClickListener { openAppSettings() }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    // ===== 顶部栏导航 =====
    private fun setupTopBar() {
        binding.btnLanguage.setOnClickListener {
            startActivity(Intent(this, LanguageSelectActivity::class.java))
        }
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    // ===== 模式切换 =====
    private fun setupModeToggle() {
        binding.modeToggleGroup.addOnButtonCheckedListener { _, checkedId, checked ->
            if (checked) when (checkedId) {
                R.id.btnRealtimeMode -> { currentMode = Mode.LIVE; setupLiveMode() }
                R.id.btnCaptureMode -> { currentMode = Mode.PHOTO; setupPhotoMode() }
            }
        }
    }

    private fun setupLiveMode() {
        binding.captureButton.visibility = android.view.View.GONE
        binding.textOverlay.visibility = android.view.View.VISIBLE
        binding.statusBar.visibility = android.view.View.VISIBLE
        updateStatus(getString(R.string.status_live_ready))
    }

    private fun setupPhotoMode() {
        binding.textOverlay.visibility = android.view.View.GONE
        binding.captureButton.visibility = android.view.View.VISIBLE
        binding.statusBar.visibility = android.view.View.GONE
    }

    private fun setupCaptureButton() {
        binding.captureButton.setOnClickListener {
            if (currentMode == Mode.PHOTO) takePhoto()
        }
    }

    // ===== 相机启动 =====
    private fun startCamera() {
        if (cameraStarted.getAndSet(true)) return
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(Size(1280, 720))
                    .build()
                    .also { it.setAnalyzer(cameraExecutor) { imageProxy -> processFrame(imageProxy) } }

                imageCapture = ImageCapture.Builder()
                    .setTargetResolution(Size(1920, 1080))
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis, imageCapture
                )
                Log.d("CameraActivity", "CameraX started")
            } catch (exc: Exception) {
                Log.e("CameraActivity", "Camera binding failed", exc)
                mainHandler.post {
                    Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // ===== 实时识别 =====
    private fun processFrame(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastOcrTime < ocrThrottleInterval) {
            imageProxy.close()
            return
        }
        lastOcrTime = currentTime

        cameraExecutor.execute {
            // analyze 在 finally 中关闭 imageProxy（仓库独占其生命周期），调用方不再关闭
            val ocrResult = ocrUseCase.analyze(imageProxy)
            mainHandler.post { handleOcrResult(ocrResult) }
        }
    }

    private fun handleOcrResult(ocrResult: OcrResult) {
        if (ocrResult.success && ocrResult.textRecognitionResult != null) {
            val fullText = ocrResult.textRecognitionResult.fullText
            binding.originalText.text = if (fullText.length > 100) {
                fullText.substring(0, 100) + "…"
            } else fullText

            if (currentMode == Mode.LIVE) translateLive(fullText)
        } else {
            binding.originalText.text = ""
            binding.translatedText.text = getString(R.string.status_no_text)
            updateStatus(getString(R.string.status_no_text))
        }
    }

    private fun translateLive(text: String) {
        val previous = previousText.get()
        if (text == previous) return
        previousText.set(text)
        updateStatus(getString(R.string.status_translating))

        lifecycleScope.launch(Dispatchers.IO) {
            val target = LanguagePreferences.getTarget(this@CameraActivity)
            val result = translateUseCase.translate(text, "auto", target)
            mainHandler.post {
                if (result.errorMessage != null) {
                    binding.translatedText.text = result.errorMessage
                } else {
                    binding.translatedText.text = result.translatedText
                }
                updateStatus(getString(R.string.status_live_ready))
            }
        }
    }

    // ===== 拍照模式 =====
    private fun takePhoto() {
        val imageCapture = imageCapture ?: run {
            Toast.makeText(this, R.string.processing, Toast.LENGTH_SHORT).show()
            return
        }
        binding.loadingOverlay.visibility = android.view.View.VISIBLE
        imageCapture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = image.toBitmap()
                image.close()
                lifecycleScope.launch(Dispatchers.IO) {
                    val ocr = ocrUseCase.analyze(bitmap)
                    val source = LanguagePreferences.getSource(this@CameraActivity)
                    val target = LanguagePreferences.getTarget(this@CameraActivity)
                    if (ocr.success && ocr.textRecognitionResult != null) {
                        val fullText = ocr.textRecognitionResult.fullText
                        val translation = translateUseCase.translate(fullText, source, target)
                        mainHandler.post {
                            binding.loadingOverlay.visibility = android.view.View.GONE
                            openResult(fullText, translation.originalLanguage, target, translation)
                        }
                    } else {
                        mainHandler.post {
                            binding.loadingOverlay.visibility = android.view.View.GONE
                            openResult("", "auto", target, null)
                        }
                    }
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraActivity", "Photo capture failed", exception)
                mainHandler.post {
                    binding.loadingOverlay.visibility = android.view.View.GONE
                    Toast.makeText(this@CameraActivity, R.string.translation_failed, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun openResult(
        original: String,
        sourceLang: String,
        targetLang: String,
        translation: com.example.phototranslate.domain.TranslationResult?
    ) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("original_text", original)
            putExtra("translated_text", translation?.translatedText ?: "")
            putExtra("source_lang", sourceLang)
            putExtra("target_lang", targetLang)
            putExtra("is_error", translation?.errorMessage != null)
        }
        startActivity(intent)
    }

    // ===== UI 辅助 =====
    private fun updateStatus(text: String) {
        mainHandler.post { binding.statusText.text = text }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
