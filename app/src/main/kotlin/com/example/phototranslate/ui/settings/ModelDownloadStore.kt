package com.example.phototranslate.ui.settings

import android.content.Context

/**
 * 本地记录「用户已下载」的语言模型集合，仅用于模型管理 UI 的状态反馈。
 * 真正的 ML Kit 模型仍会在首次翻译时按需下载。
 */
object ModelDownloadStore {

    private const val PREFS_NAME = "photo_translate_models"
    private const val KEY_DOWNLOADED = "downloaded_models"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isDownloaded(context: Context, code: String): Boolean {
        return prefs(context).getStringSet(KEY_DOWNLOADED, emptySet())?.contains(code) == true
    }

    fun markDownloaded(context: Context, code: String) {
        val set = prefs(context).getStringSet(KEY_DOWNLOADED, emptySet())?.toMutableSet() ?: mutableSetOf()
        set.add(code)
        prefs(context).edit().putStringSet(KEY_DOWNLOADED, set).apply()
    }

    fun markDeleted(context: Context, code: String) {
        val set = prefs(context).getStringSet(KEY_DOWNLOADED, emptySet())?.toMutableSet() ?: mutableSetOf()
        set.remove(code)
        prefs(context).edit().putStringSet(KEY_DOWNLOADED, set).apply()
    }
}
