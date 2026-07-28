package com.example.phototranslate.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phototranslate.data.history.TranslationHistoryEntity
import com.example.phototranslate.usecase.HistoryUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * HistoryViewModel - ViewModel for the history screen.
 * Provides history data to the UI and handles history operations.
 */
class HistoryViewModel(private val historyUseCase: HistoryUseCase) : ViewModel() {

    // Flow of all history entries (collected by UI)
    val allEntries = historyUseCase.getHistory()

    // Flow of favorite entries
    val favorites = historyUseCase.getFavorites()

    // History count
    val count = historyUseCase.getHistoryCount()

    /**
     * Save a new translation entry to history.
     */
    fun saveHistory(original: String, translated: String, source: String, target: String) {
        viewModelScope.launch {
            historyUseCase.saveHistory(original, translated, source, target)
        }
    }

    /**
     * Search history entries.
     */
    fun search(query: String) = historyUseCase.searchHistory(query)

    /**
     * Delete a specific entry.
     */
    fun deleteEntry(entry: TranslationHistoryEntity) {
        viewModelScope.launch {
            historyUseCase.deleteHistoryEntry(entry)
        }
    }

    /**
     * Toggle favorite status.
     */
    fun toggleFavorite(entry: TranslationHistoryEntity) {
        viewModelScope.launch {
            historyUseCase.toggleFavorite(entry)
        }
    }

    /**
     * Clear all history.
     */
    fun clearAll() {
        viewModelScope.launch {
            historyUseCase.deleteAllHistory()
        }
    }
}
