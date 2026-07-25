package com.lantianhcgp.readlater.ui.addlink

import com.lantianhcgp.readlater.agent.ProcessStep

data class AddLinkUiState(
    val url: String = "",
    val isProcessing: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val currentStep: ProcessStep? = null,
    val stepMessage: String = ""
)
