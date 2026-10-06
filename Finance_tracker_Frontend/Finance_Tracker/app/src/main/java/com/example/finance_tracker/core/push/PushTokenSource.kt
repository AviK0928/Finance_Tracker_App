package com.example.finance_tracker.core.push

import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Where the FCM registration token comes from; an interface so PushRegistrar can be tested on the JVM. */
interface PushTokenSource {

    /** The current FCM token, or null when Firebase cannot provide one (offline, no Play services). */
    suspend fun currentToken(): String?

    /** Invalidates the token at FCM, so later sends to it fail with UNREGISTERED. Best effort. */
    suspend fun deleteToken()
}

class FirebasePushTokenSource : PushTokenSource {

    override suspend fun currentToken(): String? =
        FirebaseMessaging.getInstance().token.awaitOrNull()

    override suspend fun deleteToken() {
        FirebaseMessaging.getInstance().deleteToken().awaitOrNull()
    }

    /** Completes with the task result, or null when the task failed or was cancelled. */
    private suspend fun <T> Task<T>.awaitOrNull(): T? = suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            cont.resume(if (task.isSuccessful) task.result else null)
        }
    }
}
