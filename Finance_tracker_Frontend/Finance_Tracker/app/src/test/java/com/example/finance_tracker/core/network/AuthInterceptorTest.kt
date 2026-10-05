package com.example.finance_tracker.core.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthInterceptorTest {

    @Test
    fun unauthorized_onApiCall_endsSession() {
        assertTrue(AuthInterceptor.endsSession(401, "/api/budgets"))
    }

    @Test
    fun unauthorized_onLogin_doesNotEndSession() {
        // Wrong password is also 401; it must stay an error message on the login screen
        assertFalse(AuthInterceptor.endsSession(401, "/api/auth/login"))
    }

    @Test
    fun forbidden_doesNotEndSession() {
        // 403 is "not your resource", the token itself is still valid
        assertFalse(AuthInterceptor.endsSession(403, "/api/budgets/5"))
    }
}
