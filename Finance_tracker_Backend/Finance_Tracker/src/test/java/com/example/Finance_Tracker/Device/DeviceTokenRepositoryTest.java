package com.example.Finance_Tracker.Device;

import com.example.Finance_Tracker.Device.entity.DevicePlatform;
import com.example.Finance_Tracker.Device.entity.DeviceToken;
import com.example.Finance_Tracker.Device.repository.DeviceTokenRepository;
import com.example.Finance_Tracker.User.entity.User;
import com.example.Finance_Tracker.User.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Device tokens against the dev Postgres (V5 table); each test is rolled back. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DeviceTokenRepositoryTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 6, 12, 0);

    @Autowired private DeviceTokenRepository deviceTokenRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void deleteByUserIdAndToken_deletesOnlyThatUsersToken() {
        Long me = newUser();
        Long other = newUser();
        String mine = "mine-" + System.nanoTime();
        String theirs = "theirs-" + System.nanoTime();
        deviceTokenRepository.save(new DeviceToken(me, mine, DevicePlatform.ANDROID, NOW));
        deviceTokenRepository.save(new DeviceToken(other, theirs, DevicePlatform.ANDROID, NOW));

        assertThat(deviceTokenRepository.deleteByUserIdAndToken(me, theirs)).isZero();
        assertThat(deviceTokenRepository.deleteByUserIdAndToken(me, mine)).isOne();
        assertThat(deviceTokenRepository.findByUserId(me)).isEmpty();
        assertThat(deviceTokenRepository.findByUserId(other)).extracting(DeviceToken::getToken).containsExactly(theirs);
    }

    @Test
    void sameTokenTwice_isRejectedByTheDatabase() {
        Long me = newUser();
        Long other = newUser();
        String token = "dup-" + System.nanoTime();
        deviceTokenRepository.saveAndFlush(new DeviceToken(me, token, DevicePlatform.ANDROID, NOW));

        assertThatThrownBy(() -> deviceTokenRepository.saveAndFlush(
                new DeviceToken(other, token, DevicePlatform.ANDROID, NOW)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Long newUser() {
        return userRepository.save(User.builder()
                .email("device-" + System.nanoTime() + "@example.com")
                .username("device")
                .password("not-a-real-hash")
                .build()).getId();
    }
}
