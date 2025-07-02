package com.example.finance_tracker.features.settings.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finance_tracker.core.network.model.settings.SettingKey

@Composable
fun SettingItem(
    key: SettingKey,
    value: String,
    onUpdate: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = key.name, style = MaterialTheme.typography.labelMedium)
        OutlinedTextField(
            value = value,
            onValueChange = onUpdate,
            modifier = Modifier.fillMaxWidth(),
            interactionSource = interactionSource
        )
    }
}