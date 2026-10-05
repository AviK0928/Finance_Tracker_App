package com.example.finance_tracker.core.sync

import com.example.finance_tracker.core.network.NetworkResult

interface SyncRepo {

    /** Pulls changes since the stored cursor (everything on the first run) into the local database. */
    suspend fun sync(): NetworkResult<Unit>

    /** Removes all local data, including the cursor. Used when the session ends. */
    suspend fun clearLocalData()
}
