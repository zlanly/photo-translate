package com.example.phototranslate.usecase

import android.content.Context
import com.example.phototranslate.repository.TranslateRepository
import com.example.phototranslate.repository.OcrRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 回归测试：翻译自动检测透传。
 * 旧实现里 detectLanguage 写死返回 "und"，导致 "auto" 被当成源语言、仓库又回退成英文，
 * 自动检测实际失效。修复后 sourceLanguageCode 应原样透传给仓库。
 */
class DefaultTranslateUseCaseTest {

    @Test
    fun translate_passesAutoSourceThrough_toRepository() {
        val repo = mockk<TranslateRepository>(relaxed = true)
        val ocr = mockk<OcrUseCase>(relaxed = true)
        val context = mockk<Context>(relaxed = true)

        val sourceSlot = slot<String>()
        every { repo.isModelInstalled("zh") } returns true
        every { repo.translate("hello", capture(sourceSlot), "zh") } returns Result.success("你好")

        val useCase = DefaultTranslateUseCase(repo, ocr, context)
        val result = useCase.translate("hello", "auto", "zh")

        // 1. 源语言必须原样透传 "auto"，绝不能是 "und"
        assertEquals("auto", sourceSlot.captured)
        assertEquals("auto", result.originalLanguage)
        // 2. 译文正确映射
        assertEquals("你好", result.translatedText)
        assertEquals(null, result.errorMessage)
    }

    @Test
    fun translate_mapsFailure_toErrorResult() {
        val repo = mockk<TranslateRepository>(relaxed = true)
        val ocr = mockk<OcrUseCase>(relaxed = true)
        val context = mockk<Context>(relaxed = true)

        every { repo.isModelInstalled("zh") } returns true
        every { repo.translate("hello", "auto", "zh") } returns Result.failure(RuntimeException("boom"))

        val useCase = DefaultTranslateUseCase(repo, ocr, context)
        val result = useCase.translate("hello", "auto", "zh")

        assertEquals("boom", result.errorMessage)
        assertEquals("", result.translatedText)
    }
}
