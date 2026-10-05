package com.example.finance_tracker.core.sync

import com.example.finance_tracker.core.di_config.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the local copy in step with the session:
 * - a session starts (app start with a stored token, or a login): sync;
 * - a session ends (logout, account deletion, a 401 from any request): wipe the local copy, so the
 *   next person to log in on this phone never sees the previous user's data. Also wipes when the
 *   app starts logged out, in case it was killed before an earlier wipe ran.
 * Sync and wipe share SyncRepoImpl's lock, so they run in the order they were asked for.
 */
@Singleton
class SessionSyncCoordinator @Inject constructor(
    private val syncRepo: SyncRepo,
    @ApplicationScope private val scope: CoroutineScope
) : SyncTrigger {

    @Volatile
    private var loggedIn = false

    fun follow(isLoggedIn: Flow<Boolean>) {
        scope.launch {
            isLoggedIn.distinctUntilChanged().collect { now ->
                loggedIn = now
                if (now) syncRepo.sync() else syncRepo.clearLocalData()
            }
        }
    }

    /** Refreshes the local copy after a change made online. Ignored while logged out. */
    override fun requestSync() {
        if (loggedIn) {
            scope.launch { syncRepo.sync() }
        }
    }
}
