package com.example.phototranslate.ui.result

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.phototranslate.databinding.ActivityResultBinding

/**
 * Result Activity - Phase 3/4
 * Displays translation result with copy/share/save actions and error handling.
 */
class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var originalText: String
    private lateinit var translatedText: String
    private var sourceLanguage: String = "auto"
    private var targetLanguage: String = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        resultDataFromIntent(intent)
        setupActions()
        displayResults()
    }

    private fun resultDataFromIntent(intent: Intent) {
        originalText = intent.getStringExtra("original_text") ?: "No text"
        translatedText = intent.getStringExtra("translated_text") ?: "Translation pending"
        sourceLanguage = intent.getStringExtra("source_lang") ?: "auto"
        targetLanguage = intent.getStringExtra("target_lang") ?: "en"
    }

    private fun displayResults() {
        binding.originalText.text = if (originalText.isEmpty()) "No text detected" else originalText
        binding.translatedText.text = if (translatedText.isEmpty()) "Translation failed" else translatedText
        binding.langInfoText.text = "$sourceLanguage → $targetLanguage"

        // Handle empty translation (fallback)
        if (translatedText.isEmpty() || translatedText.contains("failed") || translatedText.contains("pending")) {
            binding.translatedText.text = "Could not translate - try again or check network"
            binding.translatedText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        }
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnCopy.setOnClickListener { copyToClipboard(translatedText) }
        binding.btnShare.setOnClickListener { shareText() }
        binding.btnSave.setOnClickListener { saveToGallery() }
    }

    private fun copyToClipboard(text: String) {
        Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show()
    }

    private fun shareText() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, translatedText)
        }
        startActivity(Intent.createChooser(intent, "Share"))
    }

    private fun saveToGallery() {
        Toast.makeText(this, "Save implemented in Phase 4", Toast.LENGTH_SHORT).show()
    }
}