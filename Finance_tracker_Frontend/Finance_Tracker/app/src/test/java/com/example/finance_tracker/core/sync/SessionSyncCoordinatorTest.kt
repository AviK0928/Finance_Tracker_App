package com.example.finance_tracker.core.sync

import com.example.finance_tracker.core.network.NetworkResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionSyncCoordinatorTest {

    private val repo = RecordingSyncRepo()
    private val scope = TestScope(UnconfinedTestDispatcher())
    private val coordinator = SessionSyncCoordinator(repo, scope)

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun startingLoggedOut_wipesLeftoverLocalData() {
        coordinator.follow(MutableStateFlow(false))

        assertEquals(listOf("clear"), repo.calls)
    }

    @Test
    fun login_syncs_andLogout_wipes() {
        val loggedIn = MutableStateFlow(false)
        coordinator.follow(loggedIn)

        loggedIn.value = true
        loggedIn.value = false

        assertEquals(listOf("clear", "sync", "clear"), repo.calls)
    }

    @Test
    fun theSameLoginStateTwice_syncsOnlyOnce() {
        coordinator.follow(flowOf(true, true))

        assertEquals(listOf("sync"), repo.calls)
    }

    @Test
    fun requestSync_runsOnlyWhileLoggedIn() {
        val loggedIn = MutableStateFlow(false)
        coordinator.follow(loggedIn)
        coordinator.requestSync()
        assertEquals(listOf("clear"), repo.calls)

        loggedIn.value = true
        coordinator.requestSync()

        assertEquals(listOf("clear", "sync", "sync"), repo.calls)
    }

    private class RecordingSyncRepo : SyncRepo {
        val calls = mutableListOf<String>()

        override suspend fun sync(): NetworkResult<Unit> {
            calls += "sync"
            return NetworkResult.Success(Unit)
        }

        override suspend fun clearLocalData() {
            calls += "clear"
        }
    }
}
