package com.lantianhcgp.readlater.util

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel { DEBUG, INFO, WARN, ERROR }

data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String
)

object Logger {
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private const val MAX_LOGS = 200

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
    }

    private fun addLog(level: LogLevel, tag: String, message: String) {
        if (!_isEnabled.value) return

        val entry = LogEntry(level = level, tag = tag, message = message)
        val current = _logs.value.toMutableList()
        current.add(entry)
        if (current.size > MAX_LOGS) {
            current.removeAt(0)
        }
        _logs.value = current

        when (level) {
            LogLevel.DEBUG -> Log.d(tag, message)
            LogLevel.INFO -> Log.i(tag, message)
            LogLevel.WARN -> Log.w(tag, message)
            LogLevel.ERROR -> Log.e(tag, message)
        }
    }

    fun d(tag: String, message: String) = addLog(LogLevel.DEBUG, tag, message)
    fun i(tag: String, message: String) = addLog(LogLevel.INFO, tag, message)
    fun w(tag: String, message: String) = addLog(LogLevel.WARN, tag, message)
    fun e(tag: String, message: String) = addLog(LogLevel.ERROR, tag, message)

    fun clear() {
        _logs.value = emptyList()
    }

    fun getFormattedLogs(): String {
        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        return _logs.value.joinToString("\n") { entry ->
            "[${sdf.format(Date(entry.timestamp))}] ${entry.level.name}/${entry.tag}: ${entry.message}"
        }
    }
}
