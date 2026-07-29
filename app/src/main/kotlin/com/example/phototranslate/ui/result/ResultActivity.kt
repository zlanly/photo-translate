package com.example.phototranslate.ui.result

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.phototranslate.R
import com.example.phototranslate.databinding.ActivityResultBinding
import com.example.phototranslate.data.history.AppDatabase
import com.example.phototranslate.domain.ALL_LANGUAGE_OPTIONS
import com.example.phototranslate.repository.DefaultHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 结果页：展示原文 / 译文，支持复制、分享、保存到历史。
 */
class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    private var originalText: String = ""
    private var translatedText: String = ""
    private var sourceLanguage: String = "auto"
    private var targetLanguage: String = "zh"
    private var isError: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        resultDataFromIntent(intent)
        setupActions()
        displayResults()
    }

    private fun resultDataFromIntent(intent: Intent) {
        originalText = intent.getStringExtra("original_text") ?: ""
        translatedText = intent.getStringExtra("translated_text") ?: ""
        sourceLanguage = intent.getStringExtra("source_lang") ?: "auto"
        targetLanguage = intent.getStringExtra("target_lang") ?: "zh"
        isError = intent.getBooleanExtra("is_error", false)
    }

    private fun displayResults() {
        binding.originalText.text =
            if (originalText.isBlank()) getString(R.string.no_text_detected) else originalText

        if (isError || translatedText.isBlank()) {
            binding.translatedText.text = getString(R.string.translation_failed)
            binding.translatedText.setTextColor(
                ContextCompat.getColor(this, android.R.color.holo_red_dark)
            )
        } else {
            binding.translatedText.text = translatedText
            binding.translatedText.setTextColor(
                ContextCompat.getColor(this, R.color.on_surface)
            )
        }

        binding.langInfoText.text = getString(
            R.string.source_target_info,
            languageDisplayName(sourceLanguage),
            languageDisplayName(targetLanguage)
        )
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnCopy.setOnClickListener { copyToClipboard(translatedText) }
        binding.btnShare.setOnClickListener { shareText() }
        binding.btnSave.setOnClickListener { saveToHistory() }
    }

    private fun copyToClipboard(text: String) {
        if (text.isBlank()) {
            Toast.makeText(this, R.string.translation_failed, Toast.LENGTH_SHORT).show()
            return
        }
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = android.content.ClipData.newPlainText(getString(R.string.translated_text), text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, R.string.toast_copied, Toast.LENGTH_SHORT).show()
    }

    private fun shareText() {
        if (translatedText.isBlank()) {
            Toast.makeText(this, R.string.translation_failed, Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, translatedText)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    private fun saveToHistory() {
        if (originalText.isBlank()) {
            Toast.makeText(this, R.string.no_text_detected, Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val repo = DefaultHistoryRepository(AppDatabase.getDatabase(this@ResultActivity))
            repo.saveEntry(originalText, translatedText, sourceLanguage, targetLanguage, null)
            runOnUiThread {
                Toast.makeText(this@ResultActivity, R.string.toast_saved_history, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun languageDisplayName(code: String): String {
        return ALL_LANGUAGE_OPTIONS.firstOrNull { it.code == code }?.displayName ?: code
    }
}
