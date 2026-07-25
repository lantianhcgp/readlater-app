package com.lantianhcgp.readlater.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lantianhcgp.readlater.ui.theme.AmberOrange

@Composable
fun TagChip(
    name: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onClick,
        label = { Text(name) },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = AmberOrange.copy(alpha = 0.12f),
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = null
    )
}
