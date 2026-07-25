package com.lantianhcgp.readlater.agent

import com.lantianhcgp.readlater.agent.tools.FetchContentTool
import com.lantianhcgp.readlater.data.model.LlmConfig
import com.lantianhcgp.readlater.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

data class AgentResult(
    val title: String? = null,
    val summary: String? = null,
    val plainText: String? = null,
    val tags: List<String> = emptyList(),
    val imageUrl: String? = null,
    val sourceDomain: String? = null,
    val readingTimeMinutes: Int? = null,
    val error: String? = null
)

enum class ProcessStep(val displayName: String) {
    FETCHING("正在抓取网页内容..."),
    PARSING("正在解析文章结构..."),
    SUMMARIZING("AI 正在生成摘要..."),
    TAGGING("AI 正在生成标签..."),
    DONE("处理完成"),
    ERROR("处理失败")
}

@Singleton
class AgentOrchestrator @Inject constructor(
    private val fetchContentTool: FetchContentTool,
    private val toolExecutor: ToolExecutor,
    private val llmProviderFactory: LlmProviderFactory
) {

    private val _currentStep = MutableStateFlow<ProcessStep?>(null)
    val currentStep: StateFlow<ProcessStep?> = _currentStep.asStateFlow()

    private val _stepMessage = MutableStateFlow<String>("")
    val stepMessage: StateFlow<String> = _stepMessage.asStateFlow()

    suspend fun processUrl(url: String, config: LlmConfig): AgentResult = withContext(Dispatchers.IO) {
        try {
            Logger.i(TAG, "开始处理 URL: $url")
            Logger.i(TAG, "模型: ${config.model}, Provider: ${config.provider}")
            _currentStep.value = ProcessStep.FETCHING
            _stepMessage.value = "正在抓取网页内容..."

            val provider = llmProviderFactory.create(config)
            Logger.d(TAG, "LLM Provider 创建成功")

            Logger.d(TAG, "开始抓取网页内容...")
            val fetchResult = fetchContentTool.execute(url)
            val fetchJson = org.json.JSONObject(fetchResult)

            if (fetchJson.has("error")) {
                val errorMsg = "抓取失败: ${fetchJson.getString("error")}"
                Logger.e(TAG, errorMsg)
                _currentStep.value = ProcessStep.ERROR
                _stepMessage.value = errorMsg
                return@withContext AgentResult(error = errorMsg)
            }

            val title = fetchJson.optString("title", null)
            val plainText = fetchJson.optString("plainText", "")
            val imageUrl = fetchJson.optString("imageUrl", null)
            val sourceDomain = fetchJson.optString("sourceDomain", null)
            val readingTime = fetchJson.optInt("readingTimeMinutes", 0)

            Logger.i(TAG, "网页抓取成功: 标题=$title, 域名=$sourceDomain, 字数=${plainText.length}")
            _currentStep.value = ProcessStep.PARSING
            _stepMessage.value = "正在解析文章结构..."

            Logger.d(TAG, "开始生成摘要...")
            _currentStep.value = ProcessStep.SUMMARIZING
            _stepMessage.value = "AI 正在生成摘要..."

            val summary = toolExecutor.executeSummarize(plainText, title, provider)
            Logger.i(TAG, "摘要生成完成: ${summary?.take(50)}...")

            Logger.d(TAG, "开始生成标签...")
            _currentStep.value = ProcessStep.TAGGING
            _stepMessage.value = "AI 正在生成标签..."

            val tagsJson = toolExecutor.executeAutoTag(plainText, title, provider)
            Logger.i(TAG, "标签生成完成: $tagsJson")

            val tags = try {
                val arr = JSONArray(tagsJson)
                (0 until arr.length()).map { arr.getString(it) }
            } catch (_: Exception) {
                emptyList()
            }

            Logger.i(TAG, "处理完成: 标题=$title, 标签=$tags, 阅读时间=${readingTime}分钟")
            _currentStep.value = ProcessStep.DONE
            _stepMessage.value = "处理完成"

            AgentResult(
                title = title,
                summary = summary,
                plainText = plainText,
                tags = tags,
                imageUrl = imageUrl,
                sourceDomain = sourceDomain,
                readingTimeMinutes = if (readingTime > 0) readingTime else null
            )
        } catch (e: Exception) {
            Logger.e(TAG, "处理出错: ${e.message}")
            _currentStep.value = ProcessStep.ERROR
            _stepMessage.value = "处理失败: ${e.message}"
            AgentResult(error = e.message ?: "Unknown error")
        }
    }

    fun resetStep() {
        _currentStep.value = null
        _stepMessage.value = ""
    }

    companion object {
        private const val TAG = "AgentOrchestrator"
    }
}
