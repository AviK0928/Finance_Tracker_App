package com.example.finance_tracker.core.sync

/** Lets a repository ask for a background sync without depending on how it is run. */
interface SyncTrigger {
    fun requestSync()
}
