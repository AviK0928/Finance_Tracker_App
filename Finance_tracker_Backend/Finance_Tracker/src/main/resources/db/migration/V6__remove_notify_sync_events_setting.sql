-- NOTIFY_SYNC_EVENTS controlled the WebSocket sync notifications removed in phase 9a; nothing reads it.
DELETE FROM user_settings WHERE key = 'NOTIFY_SYNC_EVENTS';

-- The CHECK from V1 (default name <table>_<column>_check) still lists the key; recreate it without it.
ALTER TABLE user_settings DROP CONSTRAINT user_settings_key_check;
ALTER TABLE user_settings ADD CONSTRAINT user_settings_key_check
    CHECK (key IN ('AUTO_SYNC_ENABLED', 'SYNC_FREQUENCY_MINUTES', 'NOTIFICATIONS_ENABLED',
                   'NOTIFY_BUDGET_EXPIRY', 'NOTIFY_SPENDING_ALERTS', 'DEFAULT_BUDGET_DURATION', 'DEFAULT_CURRENCY'));
