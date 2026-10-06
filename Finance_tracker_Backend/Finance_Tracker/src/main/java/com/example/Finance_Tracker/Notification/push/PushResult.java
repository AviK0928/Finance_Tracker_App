package com.example.Finance_Tracker.Notification.push;

public enum PushResult {
    SENT,
    /** Push is not configured on this server (no service account): nothing was sent. */
    SKIPPED,
    /** Firebase says the token will never work again (app uninstalled, token from another project): drop it. */
    TOKEN_GONE,
    /** Anything else (network, quota, Firebase outage): keep the token, the next notification tries again. */
    FAILED
}
