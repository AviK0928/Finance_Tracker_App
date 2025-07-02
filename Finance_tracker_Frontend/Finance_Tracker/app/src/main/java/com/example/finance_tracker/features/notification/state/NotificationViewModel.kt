package com.example.finance_tracker.features.notification.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.features.notification.domain.NotificationRepo
import com.example.finance_tracker.features.notification.domain.NotificationWebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repo: NotificationRepo,
    private val socketManager: NotificationWebSocketManager,
    private val userId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state

    init {
        socketManager.start(userId)
        observePush()
        loadAll()
    }

    private fun observePush() {
        viewModelScope.launch {
            socketManager.newNotifications.collect { incoming ->
                _state.update {
                    it.copy(
                        notifications = listOf(incoming) + it.notifications,
                        unreadCount = it.unreadCount + 1
                    )
                }
            }
        }
    }

    fun onEvent(event: NotificationEvent) {
        when (event) {
            is NotificationEvent.LoadAll -> loadAll()
            is NotificationEvent.MarkAllAsRead -> markAllAsRead()
            is NotificationEvent.MarkAsRead -> markAsRead(event.id)
            is NotificationEvent.Delete -> delete(event.id)
            is NotificationEvent.DeleteBulk -> deleteBulk(event.ids)
            is NotificationEvent.Archive -> archive(event.id)
            is NotificationEvent.ArchiveBulk -> archiveBulk(event.ids)
            is NotificationEvent.ToggleSelection -> toggleSelection(event.id)
            is NotificationEvent.SetSelection -> setSelection(event.ids)
            is NotificationEvent.ClearSelection -> clearSelection()
            is NotificationEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadAll() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val notifResult = repo.getNotifications()
            val countResult = repo.getUnreadCount()

            when {
                notifResult is NetworkResult.Success && countResult is NetworkResult.Success -> {
                    _state.update {
                        it.copy(
                            notifications = notifResult.data,
                            unreadCount = countResult.data,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }

                notifResult is NetworkResult.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = notifResult.message)
                }

                countResult is NetworkResult.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = countResult.message)
                }

                else -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun markAllAsRead() {
        viewModelScope.launch {
            when (repo.markAllAsRead()) {
                is NetworkResult.Success -> _state.update {
                    it.copy(
                        notifications = it.notifications.map { n -> n.copy(read = true) },
                        unreadCount = 0
                    )
                }

                is NetworkResult.Error -> Unit
                else -> Unit
            }
        }
    }

    private fun markAsRead(id: Long) {
        viewModelScope.launch {
            when (repo.markAsRead(id)) {
                is NetworkResult.Success -> _state.update {
                    val updatedList = it.notifications.map { n ->
                        if (n.id == id) n.copy(read = true) else n
                    }
                    val unread = updatedList.count { n -> !n.read }
                    it.copy(notifications = updatedList, unreadCount = unread.toLong())
                }

                is NetworkResult.Error -> Unit
                else -> Unit
            }
        }
    }

    private fun delete(id: Long) {
        viewModelScope.launch {
            when (repo.delete(id)) {
                is NetworkResult.Success -> _state.update {
                    val updated = it.notifications.filterNot { n -> n.id == id }
                    val unread = updated.count { n -> !n.read }
                    it.copy(
                        notifications = updated,
                        unreadCount = unread.toLong(),
                        selectedNotifications = it.selectedNotifications - id
                    )
                }

                is NetworkResult.Error -> Unit
                else -> Unit
            }
        }
    }

    private fun deleteBulk(ids: List<Long>) {
        viewModelScope.launch {
            when (repo.deleteBulk(ids)) {
                is NetworkResult.Success -> _state.update {
                    val updated = it.notifications.filterNot { n -> n.id in ids }
                    val unread = updated.count { n -> !n.read }
                    it.copy(
                        notifications = updated,
                        unreadCount = unread.toLong(),
                        selectedNotifications = emptySet()
                    )
                }

                is NetworkResult.Error -> Unit
                else -> Unit
            }
        }
    }

    private fun archive(id: Long) {
        viewModelScope.launch {
            when (repo.archive(id)) {
                is NetworkResult.Success -> _state.update {
                    val updated = it.notifications.filterNot { n -> n.id == id }
                    val unread = updated.count { n -> !n.read }
                    it.copy(
                        notifications = updated,
                        unreadCount = unread.toLong(),
                        selectedNotifications = it.selectedNotifications - id
                    )
                }

                is NetworkResult.Error -> Unit
                else -> Unit
            }
        }
    }

    private fun archiveBulk(ids: List<Long>) {
        viewModelScope.launch {
            when (repo.archiveBulk(ids)) {
                is NetworkResult.Success -> _state.update {
                    val updated = it.notifications.filterNot { n -> n.id in ids }
                    val unread = updated.count { n -> !n.read }
                    it.copy(
                        notifications = updated,
                        unreadCount = unread.toLong(),
                        selectedNotifications = emptySet()
                    )
                }

                is NetworkResult.Error -> Unit
                else -> Unit
            }
        }
    }

    private fun toggleSelection(id: Long) {
        _state.update {
            val updated = it.selectedNotifications.toMutableSet()
            if (id in updated) updated.remove(id) else updated.add(id)
            it.copy(selectedNotifications = updated)
        }
    }

    private fun setSelection(ids: Set<Long>) {
        _state.update { it.copy(selectedNotifications = ids) }
    }

    private fun clearSelection() {
        _state.update { it.copy(selectedNotifications = emptySet()) }
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.stop()
    }
}