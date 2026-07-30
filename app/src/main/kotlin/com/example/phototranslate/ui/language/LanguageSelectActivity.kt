package com.example.phototranslate.ui.language

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.phototranslate.databinding.ActivityLanguageSelectBinding
import com.example.phototranslate.databinding.ItemLanguageBinding
import com.example.phototranslate.R
import com.example.phototranslate.domain.ALL_LANGUAGE_OPTIONS
import com.example.phototranslate.domain.LanguageOption
import com.example.phototranslate.usecase.DefaultTranslateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 语言选择页：分别选择「源语言（含自动检测）」与「目标语言」，
 * 实时写入 LanguagePreferences（默认 源=auto，目标=zh）。
 */
class LanguageSelectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageSelectBinding

    private var selectedSource: String = LanguagePreferences.DEFAULT_SOURCE
    private var selectedTarget: String = LanguagePreferences.DEFAULT_TARGET

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedSource = LanguagePreferences.getSource(this)
        selectedTarget = LanguagePreferences.getTarget(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener {
            // 保存即预热常用翻译模型，下载在设置页就开始，回到相机无需再等待。
            lifecycleScope.launch(Dispatchers.IO) {
                runCatching { DefaultTranslateUseCase().prepareModels(selectedSource, selectedTarget) }
            }
            finish()
        }

        renderList(binding.sourceList, source = true)
        renderList(binding.targetList, source = false)
    }

    private fun renderList(container: android.widget.LinearLayout, source: Boolean) {
        container.removeAllViews()
        val options = if (source) ALL_LANGUAGE_OPTIONS else ALL_LANGUAGE_OPTIONS.filter { it.code != "auto" }
        val selected = if (source) selectedSource else selectedTarget

        val inflater = LayoutInflater.from(this)
        for (option in options) {
            val row = ItemLanguageBinding.inflate(inflater, container, false)
            row.itemFlag.text = option.flagEmoji
            row.itemName.text = option.displayName
            val isSelected = option.code == selected
            row.itemCheck.visibility = if (isSelected) android.view.View.VISIBLE else android.view.View.GONE
            row.itemName.setTextColor(
                androidx.appcompat.content.res.AppCompatResources.getColorStateList(
                    this, if (isSelected) R.color.primary else R.color.on_surface
                )
            )
            row.root.setOnClickListener {
                if (source) {
                    selectedSource = option.code
                    LanguagePreferences.setSource(this, option.code)
                } else {
                    selectedTarget = option.code
                    LanguagePreferences.setTarget(this, option.code)
                }
                renderList(binding.sourceList, source = true)
                renderList(binding.targetList, source = false)
            }
            container.addView(row.root)
        }
    }
}
