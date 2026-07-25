package com.lantianhcgp.readlater.ui.addlink

data class AddLinkUiState(
    val url: String = "",
    val isProcessing: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)
