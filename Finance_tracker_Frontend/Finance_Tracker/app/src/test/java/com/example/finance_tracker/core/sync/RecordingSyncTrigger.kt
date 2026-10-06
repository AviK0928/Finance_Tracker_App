package com.example.finance_tracker.core.sync

/** Counts sync requests; shared by the ViewModel tests whose pull to refresh also asks for a sync. */
class RecordingSyncTrigger : SyncTrigger {
    var requests = 0

    override fun requestSync() {
        requests++
    }
}
