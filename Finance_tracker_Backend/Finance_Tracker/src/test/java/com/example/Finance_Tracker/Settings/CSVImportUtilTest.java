package com.example.Finance_Tracker.Settings;

import com.example.Finance_Tracker.Settings.entity.UserSetting;
import com.example.Finance_Tracker.Settings.util.CSVImportUtil;
import com.example.Finance_Tracker.Settings.util.SettingKey;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CSVImportUtilTest {

    private static InputStream csv(String... rows) {
        String text = "id,userId,key,value,updatedAt\n" + String.join("\n", rows) + "\n";
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void settings_fromAnExportMadeBeforeV6_skipTheRetiredKey() {
        List<UserSetting> settings = CSVImportUtil.parseSettingsCSV(csv(
                "1,5,NOTIFY_SYNC_EVENTS,true,2026-10-01T10:00:00",
                "2,5,NOTIFICATIONS_ENABLED,false,2026-10-01T10:00:00"));

        assertThat(settings).extracting(UserSetting::getKey).containsExactly(SettingKey.NOTIFICATIONS_ENABLED);
    }

    @Test
    void settings_withAnUnknownKey_areStillRejected() {
        assertThatThrownBy(() -> CSVImportUtil.parseSettingsCSV(csv("1,5,NOT_A_SETTING,true,2026-10-01T10:00:00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Invalid settings.csv");
    }
}
