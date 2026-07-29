package com.example.phototranslate.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.phototranslate.R
import com.example.phototranslate.databinding.ActivitySettingsBinding
import com.example.phototranslate.domain.ALL_LANGUAGE_OPTIONS
import com.example.phototranslate.domain.ModelDownloadStatus
import com.example.phototranslate.domain.ModelStatus
import com.example.phototranslate.ui.language.LanguagePreferences
import com.example.phototranslate.ui.language.LanguageSelectActivity
import com.example.phototranslate.ui.modelstatus.ModelStatusViewModel
import com.example.phototranslate.usecase.DefaultHistoryUseCase
import com.example.phototranslate.usecase.DefaultTranslateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val translateUseCase = DefaultTranslateUseCase()
    private val modelViewModel = ModelStatusViewModel(translateUseCase)
    private val supportedLanguages = translateUseCase.getSupportedLanguages()

    private val modelAdapter by lazy {
        ModelAdapter { option, status ->
            if (status == ModelDownloadStatus.INSTALLED) {
                ModelDownloadStore.markDeleted(this, option.code)
                modelViewModel.deleteModel(option.code)
            } else {
                ModelDownloadStore.markDownloaded(this, option.code)
                modelViewModel.downloadModel(option.code)
            }
            refreshModels()
        }
    }

    private var lastStatuses: List<ModelStatus> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.defaultTargetRow.setOnClickListener {
            startActivity(Intent(this, LanguageSelectActivity::class.java))
        }
        binding.clearHistoryRow.setOnClickListener { confirmClearHistory() }

        binding.modelRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.modelRecyclerView.adapter = modelAdapter

        observeModels()
    }

    override fun onResume() {
        super.onResume()
        refreshTargetLabel()
    }

    private fun refreshTargetLabel() {
        val target = LanguagePreferences.getTarget(this)
        val name = ALL_LANGUAGE_OPTIONS.firstOrNull { it.code == target }?.displayName ?: target
        binding.defaultTargetText.text = name
    }

    private fun observeModels() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                modelViewModel.modelStatuses.collect { statuses ->
                    lastStatuses = statuses
                    modelAdapter.submitList(buildRows(statuses))
                }
            }
        }
    }

    private fun refreshModels() {
        modelAdapter.submitList(buildRows(lastStatuses))
    }

    private fun buildRows(statuses: List<ModelStatus>): List<ModelRow> {
        return supportedLanguages.map { option ->
            val status = if (ModelDownloadStore.isDownloaded(this, option.code)) {
                ModelStatus(option.code, ModelDownloadStatus.INSTALLED)
            } else {
                statuses.firstOrNull { it.languageCode == option.code }
                    ?: ModelStatus(option.code, ModelDownloadStatus.NOT_INSTALLED)
            }
            ModelRow(option, status)
        }
    }

    private fun confirmClearHistory() {
        AlertDialog.Builder(this)
            .setTitle(R.string.settings_clear_history)
            .setMessage(R.string.confirm_clear_history)
            .setPositiveButton(R.string.history_clear) { _, _ ->
                lifecycleScope.launch {
                    DefaultHistoryUseCase().deleteAllHistory()
                    runOnUiThread {
                        Toast.makeText(this@SettingsActivity, R.string.history_clear, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
