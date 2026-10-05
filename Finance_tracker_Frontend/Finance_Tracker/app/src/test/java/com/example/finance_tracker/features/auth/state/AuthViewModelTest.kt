package com.example.finance_tracker.features.auth.state

import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.auth.AuthResponseDTO
import com.example.finance_tracker.core.network.model.auth.ForgotPasswordRequestDTO
import com.example.finance_tracker.core.network.model.auth.LoginRequestDTO
import com.example.finance_tracker.core.network.model.auth.MessageResponseDTO
import com.example.finance_tracker.core.network.model.auth.RegisterRequestDTO
import com.example.finance_tracker.core.network.model.auth.ResetPasswordRequestDTO
import com.example.finance_tracker.features.auth.domain.AuthRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    /** Only the session-expired members are used; the network calls are not part of these tests. */
    private class FakeAuthRepo : AuthRepo {
        override val sessionExpired = MutableStateFlow(false)
        var dismissCalls = 0

        override suspend fun dismissSessionExpired() {
            dismissCalls++
            sessionExpired.value = false
        }

        override suspend fun register(request: RegisterRequestDTO): NetworkResult<AuthResponseDTO> =
            throw UnsupportedOperationException()
        override suspend fun login(request: LoginRequestDTO): NetworkResult<AuthResponseDTO> =
            throw UnsupportedOperationException()
        override suspend fun forgotPassword(request: ForgotPasswordRequestDTO): NetworkResult<MessageResponseDTO> =
            throw UnsupportedOperationException()
        override suspend fun resetPassword(request: ResetPasswordRequestDTO): NetworkResult<MessageResponseDTO> =
            throw UnsupportedOperationException()
    }

    private val repo = FakeAuthRepo()

    @Before
    fun setUp() {
        // viewModelScope runs on Dispatchers.Main, which does not exist on the JVM
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun expiredSession_isShownOnTheLoginScreen() {
        val viewModel = AuthViewModel(repo)
        assertFalse(viewModel.state.value.sessionExpired)

        repo.sessionExpired.value = true

        assertTrue(viewModel.state.value.sessionExpired)
    }

    @Test
    fun dismiss_clearsTheStoredFlag() {
        repo.sessionExpired.value = true
        val viewModel = AuthViewModel(repo)

        viewModel.onEvent(AuthEvent.DismissSessionExpired)

        assertEquals(1, repo.dismissCalls)
        assertFalse(viewModel.state.value.sessionExpired)
    }
}
