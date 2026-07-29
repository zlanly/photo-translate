package com.example.phototranslate.ui.language

import android.content.Context

/**
 * 语言偏好（SharedPreferences 持久化）。
 * 默认源语言 = 自动检测（auto），目标语言 = 简体中文（zh），契合中文用户。
 */
object LanguagePreferences {

    private const val PREFS_NAME = "photo_translate_language"
    private const val KEY_SOURCE = "source_language"
    private const val KEY_TARGET = "target_language"

    const val DEFAULT_SOURCE = "auto"
    const val DEFAULT_TARGET = "zh"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSource(context: Context): String =
        prefs(context).getString(KEY_SOURCE, DEFAULT_SOURCE) ?: DEFAULT_SOURCE

    fun getTarget(context: Context): String =
        prefs(context).getString(KEY_TARGET, DEFAULT_TARGET) ?: DEFAULT_TARGET

    fun setSource(context: Context, code: String) {
        prefs(context).edit().putString(KEY_SOURCE, code).apply()
    }

    fun setTarget(context: Context, code: String) {
        prefs(context).edit().putString(KEY_TARGET, code).apply()
    }
}
