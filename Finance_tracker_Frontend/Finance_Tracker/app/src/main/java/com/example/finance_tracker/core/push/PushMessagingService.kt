package com.example.finance_tracker.core.push

import com.example.finance_tracker.core.sync.SyncTrigger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * FCM entry point. With the app in the background the system shows the notification itself and this
 * class only sees new tokens; in the foreground it also receives the message and refreshes the local
 * copy, so the new notification and any changed data show up.
 */
@AndroidEntryPoint
class PushMessagingService : FirebaseMessagingService() {

    @Inject lateinit var pushRegistrar: PushRegistrar
    @Inject lateinit var syncTrigger: SyncTrigger

    override fun onNewToken(token: String) {
        pushRegistrar.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (message.data[DATA_ACTION] == ACTION_SYNC) {
            syncTrigger.requestSync() // ignored while logged out
        }
    }

    companion object {
        // Set by the backend's FirebasePushSender
        const val DATA_ACTION = "action"
        const val ACTION_SYNC = "sync"
    }
}
