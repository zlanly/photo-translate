package com.example.phototranslate.ui.modelstatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phototranslate.domain.ModelStatus
import com.example.phototranslate.usecase.TranslateUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for displaying model download status.
 * Provides model status data to UI components (SettingsActivity, LanguageSelectActivity).
 */
class ModelStatusViewModel(private val translateUseCase: TranslateUseCase) : ViewModel() {

    /**
     * Flow of model statuses for all supported languages.
     * Emits the current download status (INSTALLED, DOWNLOADING, NOT_INSTALLED) for each language.
     */
    val modelStatuses = translateUseCase.getModelStatuses()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    /**
     * Download a specific language model.
     */
    fun downloadModel(languageCode: String) {
        translateUseCase.downloadModel(languageCode)
            .collect { result ->
                // Handle result (show progress, error, etc.)
            }
    }

    /**
     * Delete a specific language model.
     */
    fun deleteModel(languageCode: String) {
        translateUseCase.deleteModel(languageCode)
            .collect { result ->
                // Handle result
            }
    }
}
