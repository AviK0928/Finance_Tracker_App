package com.example.finance_tracker.features.settings.state

import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.apiendpoints.DeviceApi
import com.example.finance_tracker.core.network.model.device.DeviceRegistrationDTO
import com.example.finance_tracker.core.network.model.settings.ImportSummaryDTO
import com.example.finance_tracker.core.network.model.settings.UpdateSettingDTO
import com.example.finance_tracker.core.network.model.settings.UserSettingDTO
import com.example.finance_tracker.core.push.PushRegistrar
import com.example.finance_tracker.core.push.PushTokenSource
import com.example.finance_tracker.core.sync.SyncRepo
import com.example.finance_tracker.features.settings.domain.SettingsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val calls = mutableListOf<String>()
    private val appScope = TestScope(UnconfinedTestDispatcher())

    @Before
    fun setUp() {
        // viewModelScope runs on Dispatchers.Main, which does not exist on the JVM
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        appScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun logout_unregistersTheDeviceBeforeTheSessionIsCleared() {
        val registrar = PushRegistrar(RecordingDeviceApi(calls), FixedTokenSource("t1"), appScope)
        val viewModel = SettingsViewModel(RecordingSettingsRepo(calls), UnusedSyncRepo(), registrar)

        viewModel.onEvent(SettingsEvent.Logout)

        // DELETE /api/devices needs the JWT, which the repository's logout clears
        assertEquals(listOf("unregister:t1", "logout"), calls)
        assertFalse(viewModel.state.value.isLoading)
    }

    private class RecordingDeviceApi(private val calls: MutableList<String>) : DeviceApi {
        override suspend fun register(body: DeviceRegistrationDTO): Response<Unit> =
            throw UnsupportedOperationException()

        override suspend fun unregister(token: String): Response<Unit> {
            calls += "unregister:$token"
            return Response.success(Unit)
        }
    }

    private class FixedTokenSource(private val token: String) : PushTokenSource {
        override suspend fun currentToken(): String = token
        override suspend fun deleteToken() = throw UnsupportedOperationException()
    }

    private class RecordingSettingsRepo(private val calls: MutableList<String>) : SettingsRepo {
        override suspend fun logout(): NetworkResult<Unit> {
            calls += "logout"
            return NetworkResult.Success(Unit)
        }

        override suspend fun getSettings(): NetworkResult<List<UserSettingDTO>> =
            throw UnsupportedOperationException()
        override suspend fun updateSettings(settings: List<UpdateSettingDTO>): NetworkResult<Unit> =
            throw UnsupportedOperationException()
        override suspend fun resetToDefaults(): NetworkResult<Unit> = throw UnsupportedOperationException()
        override suspend fun deleteAccount(): NetworkResult<Unit> = throw UnsupportedOperationException()
        override suspend fun exportData(): NetworkResult<ByteArray> = throw UnsupportedOperationException()
        override suspend fun importData(file: ByteArray, filename: String): NetworkResult<ImportSummaryDTO> =
            throw UnsupportedOperationException()
    }

    private class UnusedSyncRepo : SyncRepo {
        override suspend fun sync(): NetworkResult<Unit> = throw UnsupportedOperationException()
        override suspend fun clearLocalData() = throw UnsupportedOperationException()
    }
}
