package com.lantianhcgp.readlater.debug

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PipelineSnapshot(
    val url: String = "",
    val rawHtml: String = "",
    val formattedContent: String = "",
    val cleanContent: String = "",
    val summary: String = "",
    val tags: List<String> = emptyList(),
    val title: String = "",
    val error: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

object DebugData {
    private val _lastPipeline = MutableStateFlow<PipelineSnapshot?>(null)
    val lastPipeline: StateFlow<PipelineSnapshot?> = _lastPipeline.asStateFlow()

    private val _pipelineHistory = MutableStateFlow<List<PipelineSnapshot>>(emptyList())
    val pipelineHistory: StateFlow<List<PipelineSnapshot>> = _pipelineHistory.asStateFlow()

    private const val MAX_HISTORY = 20

    fun updatePipeline(snapshot: PipelineSnapshot) {
        _lastPipeline.value = snapshot
        val history = _pipelineHistory.value.toMutableList()
        history.add(0, snapshot)
        if (history.size > MAX_HISTORY) history.removeLast()
        _pipelineHistory.value = history
    }

    fun clear() {
        _lastPipeline.value = null
        _pipelineHistory.value = emptyList()
    }
}
