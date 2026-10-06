package com.example.finance_tracker.core.push

import com.example.finance_tracker.core.network.apiendpoints.DeviceApi
import com.example.finance_tracker.core.network.model.device.DeviceRegistrationDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PushRegistrarTest {

    private val calls = mutableListOf<String>()
    private val api = RecordingDeviceApi(calls)
    private val tokens = FakeTokenSource(calls, token = "t1")
    private val scope = TestScope(UnconfinedTestDispatcher())
    private val registrar = PushRegistrar(api, tokens, scope)

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun startingLoggedIn_registersTheCurrentToken() {
        registrar.follow(MutableStateFlow(true))

        assertEquals(listOf("register:t1:ANDROID"), calls)
    }

    @Test
    fun startingLoggedOut_doesNothing() {
        registrar.follow(MutableStateFlow(false))

        assertEquals(emptyList<String>(), calls)
    }

    @Test
    fun sessionEnd_deletesTheTokenOnThePhone() {
        val loggedIn = MutableStateFlow(false)
        registrar.follow(loggedIn)

        loggedIn.value = true
        loggedIn.value = false

        assertEquals(listOf("register:t1:ANDROID", "deleteToken"), calls)
    }

    @Test
    fun noTokenFromFirebase_registersNothing() {
        tokens.token = null

        registrar.follow(MutableStateFlow(true))

        assertEquals(emptyList<String>(), calls)
    }

    @Test
    fun newToken_isRegisteredOnlyWhileLoggedIn() {
        val loggedIn = MutableStateFlow(false)
        registrar.follow(loggedIn)
        registrar.onNewToken("t2")
        assertEquals(emptyList<String>(), calls)

        loggedIn.value = true
        registrar.onNewToken("t2")

        assertEquals(listOf("register:t1:ANDROID", "register:t2:ANDROID"), calls)
    }

    @Test
    fun unregister_removesTheCurrentTokenOnTheServer() = runTest {
        registrar.unregister()

        assertEquals(listOf("unregister:t1"), calls)
    }

    @Test
    fun unregister_withoutToken_callsNothing() = runTest {
        tokens.token = null

        registrar.unregister()

        assertEquals(emptyList<String>(), calls)
    }

    @Test
    fun unregister_offline_doesNotThrow() = runTest {
        api.failure = IOException("offline")

        registrar.unregister()

        assertEquals(listOf("unregister:t1"), calls)
    }

    @Test
    fun unregister_givesUpWhenTheServerDoesNotAnswer() = runTest {
        api.hang = true

        registrar.unregister()

        // Virtual time: the call returned at the timeout instead of waiting forever
        assertTrue(currentTime <= PushRegistrar.UNREGISTER_TIMEOUT_MS)
        assertEquals(listOf("unregister:t1"), calls)
    }

    private class RecordingDeviceApi(private val calls: MutableList<String>) : DeviceApi {
        var failure: IOException? = null
        var hang = false

        override suspend fun register(body: DeviceRegistrationDTO): Response<Unit> {
            calls += "register:${body.token}:${body.platform}"
            return Response.success(Unit)
        }

        override suspend fun unregister(token: String): Response<Unit> {
            calls += "unregister:$token"
            failure?.let { throw it }
            if (hang) awaitCancellation()
            return Response.success(Unit)
        }
    }

    private class FakeTokenSource(
        private val calls: MutableList<String>,
        var token: String?
    ) : PushTokenSource {
        override suspend fun currentToken(): String? = token

        override suspend fun deleteToken() {
            calls += "deleteToken"
        }
    }
}
