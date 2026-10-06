package com.example.finance_tracker.features.settings.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.settings.ImportSummaryDTO
import com.example.finance_tracker.core.network.model.settings.UpdateSettingDTO
import com.example.finance_tracker.core.push.PushRegistrar
import com.example.finance_tracker.core.sync.SyncRepo
import com.example.finance_tracker.features.settings.domain.SettingsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepo,
    private val syncRepo: SyncRepo,
    private val pushRegistrar: PushRegistrar
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.LoadSettings -> loadSettings()
            is SettingsEvent.UpdateSettings -> updateSettings(event.settings)
            is SettingsEvent.ResetToDefaults -> resetDefaults()
            is SettingsEvent.Logout -> logout()
            is SettingsEvent.DeleteAccount -> deleteAccount()
            is SettingsEvent.ExportData -> exportData()
            is SettingsEvent.ImportData -> importData(event.file, event.filename)
            is SettingsEvent.PerformSync -> performSync()
            is SettingsEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
            is SettingsEvent.ClearInfo -> _state.update { it.copy(infoMessage = null) }
            is SettingsEvent.ExportSaved -> _state.update {
                it.copy(pendingExport = null, infoMessage = if (event.saved) "Export saved" else null)
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = settingsRepo.getSettings()) {
                is NetworkResult.Success -> _state.update { it.copy(settings = result.data, isLoading = false) }
                is NetworkResult.Error -> _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                else -> Unit
            }
        }
    }

    private fun updateSettings(settings: List<UpdateSettingDTO>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = settingsRepo.updateSettings(settings)) {
                is NetworkResult.Success -> loadSettings()
                is NetworkResult.Error -> _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                else -> Unit
            }
        }
    }

    private fun resetDefaults() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = settingsRepo.resetToDefaults()) {
                is NetworkResult.Success -> loadSettings()
                is NetworkResult.Error -> _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                else -> Unit
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // Needs the JWT, so it runs before the repository clears it; best effort, at most a few seconds
            pushRegistrar.unregister()
            // The repository clears the local session whatever the server answers; AppNavGraph sees
            // the token disappear and shows the login screen.
            settingsRepo.logout()
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun deleteAccount() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = settingsRepo.deleteAccount()) {
                is NetworkResult.Success -> {
                    // The repository cleared the token; AppNavGraph shows the login screen
                    _state.update { it.copy(isLoading = false) }
                }
                is NetworkResult.Error -> {
                    _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                }
                else -> Unit
            }
        }
    }

    private fun exportData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = settingsRepo.exportData()) {
                is NetworkResult.Success -> _state.update { it.copy(isLoading = false, pendingExport = result.data) }
                is NetworkResult.Error -> _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                else -> Unit
            }
        }
    }

    private fun importData(file: ByteArray, filename: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = settingsRepo.importData(file, filename)) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(infoMessage = result.data.toMessage()) }
                    loadSettings()
                }
                is NetworkResult.Error -> _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                else -> Unit
            }
        }
    }

    private fun ImportSummaryDTO.toMessage(): String {
        val skipped = budgetsSkipped + transactionsSkipped + settingsSkipped
        return "Imported $budgetsImported budgets, $transactionsImported transactions, " +
                "$settingsImported settings ($skipped already present)"
    }

    private fun performSync() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true) }

            val result = syncRepo.sync()

            when (result) {
                is NetworkResult.Success -> {
                    _state.update {
                        it.copy(
                            lastSync = LocalDateTime.now(),
                            isSyncing = false
                        )
                    }
                    loadSettings()
                }
                is NetworkResult.Error -> {
                    _state.update {
                        it.copy(
                            errorMessage = result.message,
                            isSyncing = false
                        )
                    }
                }
                else -> Unit
            }
        }
    }
}
