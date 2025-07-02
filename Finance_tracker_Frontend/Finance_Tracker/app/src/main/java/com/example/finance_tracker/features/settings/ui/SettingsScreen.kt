package com.example.finance_tracker.features.settings.ui

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finance_tracker.core.network.model.settings.UpdateSettingDTO
import com.example.finance_tracker.core.ui.components.ErrorMessage
import com.example.finance_tracker.core.ui.components.LoadingIndicator
import com.example.finance_tracker.features.settings.state.SettingsEvent
import com.example.finance_tracker.features.settings.state.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onLogoutOrDelete: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(uri)
            val filename = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: "imported_data.json"

            inputStream?.readBytes()?.let { bytes ->
                viewModel.onEvent(SettingsEvent.ImportData(bytes, filename))
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onLogoutOrDelete = onLogoutOrDelete
        viewModel.onEvent(SettingsEvent.LoadSettings)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                LoadingIndicator()
                return@Column
            }

            state.errorMessage?.let { error ->
                ErrorMessage(
                    message = error,
                    onDismiss = { viewModel.onEvent(SettingsEvent.ClearError) }
                )
            }

            Text("Preferences", style = MaterialTheme.typography.titleMedium)

            state.settings.forEach { setting ->
                SettingItem(
                    key = setting.key,
                    value = setting.value,
                    onUpdate = { newValue ->
                        viewModel.onEvent(
                            SettingsEvent.UpdateSettings(
                                listOf(UpdateSettingDTO(setting.key, newValue))
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Data Management", style = MaterialTheme.typography.titleMedium)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { viewModel.onEvent(SettingsEvent.ExportData) },
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    Text("Export Data")
                }

                OutlinedButton(
                    onClick = { importLauncher.launch("*/*") },
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    Text("Import Data")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Account", style = MaterialTheme.typography.titleMedium)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.onEvent(SettingsEvent.Logout) },
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    Text("Logout")
                }

                OutlinedButton(
                    onClick = { viewModel.onEvent(SettingsEvent.DeleteAccount) },
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    Text("Delete Account")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Sync", style = MaterialTheme.typography.titleMedium)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Last Sync: ${state.lastSync ?: "Never"}")

                Button(
                    onClick = {
                        state.lastSync?.let {
                            viewModel.onEvent(SettingsEvent.PerformSync(it, manual = true))
                        }
                    },
                    enabled = !state.isSyncing,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    Text(if (state.isSyncing) "Syncing..." else "Sync Now")
                }
            }
        }
    }
}