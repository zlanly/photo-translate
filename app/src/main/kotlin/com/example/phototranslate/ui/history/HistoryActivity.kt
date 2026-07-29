package com.example.phototranslate.ui.history

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.phototranslate.data.history.TranslationHistoryEntity
import com.example.phototranslate.databinding.ActivityHistoryBinding
import com.example.phototranslate.R
import com.example.phototranslate.ui.result.ResultActivity
import com.example.phototranslate.usecase.DefaultHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val viewModel: HistoryViewModel by lazy { HistoryViewModel(DefaultHistoryUseCase()) }
    private val searchQuery = MutableStateFlow("")

    private val adapter by lazy {
        HistoryAdapter(
            onItemClick = { openResult(it) },
            onFavorite = {
                lifecycleScope.launch { viewModel.toggleFavorite(it) }
            },
            onDelete = {
                lifecycleScope.launch { viewModel.deleteEntry(it) }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnClear.setOnClickListener { confirmClear() }
        binding.searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery.value = s?.toString() ?: ""
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = adapter

        observe()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchQuery.flatMapLatest { q ->
                    if (q.isBlank()) viewModel.allEntries else viewModel.search(q)
                }.collect { list ->
                    adapter.submitList(list)
                    binding.emptyView.visibility =
                        if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                }
            }
        }
    }

    private fun confirmClear() {
        AlertDialog.Builder(this)
            .setTitle(R.string.history_clear)
            .setMessage(R.string.confirm_clear_history)
            .setPositiveButton(R.string.history_clear) { _, _ ->
                lifecycleScope.launch {
                    viewModel.clearAll()
                    runOnUiThread {
                        Toast.makeText(this@HistoryActivity, R.string.history_clear, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun openResult(item: TranslationHistoryEntity) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("original_text", item.originalText)
            putExtra("translated_text", item.translatedText)
            putExtra("source_lang", item.sourceLanguage)
            putExtra("target_lang", item.targetLanguage)
            putExtra("is_error", item.translatedText.isBlank())
        }
        startActivity(intent)
    }
}
