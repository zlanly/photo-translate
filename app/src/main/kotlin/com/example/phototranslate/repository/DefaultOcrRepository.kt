package com.example.phototranslate.repository

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.example.phototranslate.domain.OcrResult
import com.example.phototranslate.domain.Rect
import com.example.phototranslate.domain.TextBlock
import com.example.phototranslate.domain.TextRecognitionResult
import com.example.phototranslate.application.PhotoTranslateApp
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.Text.TextBlock as MlKitTextBlock
import com.google.mlkit.vision.text.TextRecognizer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Default implementation of OcrRepository using ML Kit Text Recognition.
 *
 * 同时运行「拉丁识别器」与「中日韩(CJK)识别器」并合并结果：
 *  - 拉丁识别器对英文/法文等拉丁字母最准；
 *  - CJK 识别器覆盖中文/日文/韩文（也能读拉丁，但精度低于前者）。
 * 合并策略：完整保留 CJK 识别到的文本块，再补入不与任何 CJK 块重叠的拉丁块，
 * 既避免重复、又让每种文字都走最擅长它的识别器，从而显著提升翻译质量。
 */
class DefaultOcrRepository(
    private val latinClient: TextRecognizer,
    private val cjkClient: TextRecognizer,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : OcrRepository {

    override fun analyze(bitmap: Bitmap): OcrResult {
        return try {
            // 两份 InputImage 各自包裹同一 Bitmap；识别器处理的是 InputImage 而非 Bitmap 本身。
            val latinImage = InputImage.fromBitmap(bitmap, 0)
            val cjkImage = InputImage.fromBitmap(bitmap, 0)
            val (blocks, dominant) = recognizeBoth(latinImage, cjkImage)
            val fullText = blocks.joinToString(" ") { it.text }
            OcrResult(
                success = true,
                textRecognitionResult = TextRecognitionResult(
                    blocks = blocks,
                    fullText = fullText,
                    dominantLanguage = dominant
                )
            )
        } catch (t: Throwable) {
            handleOcrError(t)
        }
    }

    override fun analyze(imageProxy: ImageProxy): OcrResult {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return OcrResult(success = false, errorMessage = "ImageProxy.image is null")
        }
        return try {
            val rotation = imageProxy.imageInfo.rotationDegrees
            val latinImage = InputImage.fromMediaImage(mediaImage, rotation)
            val cjkImage = InputImage.fromMediaImage(mediaImage, rotation)
            val (blocks, dominant) = recognizeBoth(latinImage, cjkImage)
            val fullText = blocks.joinToString(" ") { it.text }
            OcrResult(
                success = true,
                textRecognitionResult = TextRecognitionResult(
                    blocks = blocks,
                    fullText = fullText,
                    dominantLanguage = dominant
                )
            )
        } catch (t: Throwable) {
            handleOcrError(t)
        } finally {
            imageProxy.close() // Always close the proxy after processing
        }
    }

    /**
     * 同时跑两个识别器，合并文本块并推断占主导的语种。
     */
    private fun recognizeBoth(latinImage: InputImage, cjkImage: InputImage): Pair<List<TextBlock>, String?> {
        val latinResult = Tasks.await(latinClient.process(latinImage))
        val cjkResult = Tasks.await(cjkClient.process(cjkImage))
        val latinBlocks = latinResult.textBlocks.map { createTextBlock(it) }
        val cjkBlocks = cjkResult.textBlocks.map { createTextBlock(it) }
        val merged = mergeBlocks(cjkBlocks, latinBlocks)
        val dominant = dominantLanguage(merged)
        return merged to dominant
    }

    /**
     * 合并两个识别器的文本块：
     *  - CJK 块全部保留（拉丁识别器读不了它们，必须靠 CJK 识别器）；
     *  - 拉丁块仅在不与任何 CJK 块重叠时补入，避免把同一段英文重复计入。
     * 最后按阅读顺序（先上后下、同行左到右）排序，保证翻译输入连贯。
     */
    private fun mergeBlocks(cjk: List<TextBlock>, latin: List<TextBlock>): List<TextBlock> {
        val result = cjk.toMutableList()
        for (l in latin) {
            val lb = l.boundingBox
            // 空框（无 boundingBox）无法判断重叠，直接保留。
            if (lb.left == 0 && lb.top == 0 && lb.right == 0 && lb.bottom == 0) {
                result.add(l)
                continue
            }
            val overlaps = cjk.any { overlaps(lb, it.boundingBox, 0.4f) }
            if (!overlaps) result.add(l)
        }
        return result.sortedWith(compareBy({ it.boundingBox.top }, { it.boundingBox.left }))
    }

    /**
     * 两个矩形交叠比例（交集面积 / 较小矩形面积）是否超过阈值。
     */
    private fun overlaps(a: Rect, b: Rect, threshold: Float): Boolean {
        val ix = maxOf(a.left, b.left)
        val iy = maxOf(a.top, b.top)
        val ax = minOf(a.right, b.right)
        val ay = minOf(a.bottom, b.bottom)
        val iw = (ax - ix).toFloat()
        val ih = (ay - iy).toFloat()
        if (iw <= 0 || ih <= 0) return false
        val inter = iw * ih
        val areaA = maxOf(1f, ((a.right - a.left) * (a.bottom - a.top)).toFloat())
        val areaB = maxOf(1f, ((b.right - b.left) * (b.bottom - b.top)).toFloat())
        val small = minOf(areaA, areaB)
        return inter / small > threshold
    }

    /**
     * 按各文本块识别语种、并以字符数加权，推断整图主导语种。
     * 该语种直接作为翻译源语言，比 LanguageIdentification 更准、且零额外开销。
     */
    private fun dominantLanguage(blocks: List<TextBlock>): String? {
        val weight = mutableMapOf<String, Int>()
        for (b in blocks) {
            val lang = b.language
            if (lang.isNullOrBlank() || lang == "und") continue
            weight[lang] = weight.getOrDefault(lang, 0) + b.text.length
        }
        return weight.maxByOrNull { it.value }?.key
    }

    private fun createTextBlock(mlKitBlock: MlKitTextBlock): TextBlock {
        val boundingBox = mlKitBlock.boundingBox
        return TextBlock(
            text = mlKitBlock.text,
            boundingBox = Rect(
                left = boundingBox?.left ?: 0,
                top = boundingBox?.top ?: 0,
                right = boundingBox?.right ?: 0,
                bottom = boundingBox?.bottom ?: 0
            ),
            language = mlKitBlock.recognizedLanguage,
            confidence = 1.0f
        )
    }

    override fun identifyLanguage(text: String): String? {
        // 源语言改由 OCR 块的 recognizedLanguage 推断（见 dominantLanguage），不再依赖此接口。
        return null
    }

    override fun getConfig(): Map<String, Any> = mapOf(
        "model_type" to "ON_DEVICE",
        "performance_mode" to "FAST",
        "language_detection" to true,
        "version" to "16.0.0",
        "recognizers" to "latin+cjk"
    )

    override fun shutdown() {
        // ML Kit 客户端按需复用，无需显式关闭。
    }

    private fun handleOcrError(exception: Throwable): OcrResult {
        return OcrResult(
            success = false,
            errorMessage = "OCR error: ${exception.message}",
            exception = if (exception is Exception) exception else RuntimeException(exception)
        )
    }
}
