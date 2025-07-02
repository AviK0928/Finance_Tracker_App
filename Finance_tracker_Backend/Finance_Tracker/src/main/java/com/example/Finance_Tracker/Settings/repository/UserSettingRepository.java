package com.example.Finance_Tracker.Settings.repository;

import com.example.Finance_Tracker.Settings.entity.UserSetting;
import com.example.Finance_Tracker.Settings.util.SettingKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {
    Optional<UserSetting> findByUserIdAndKey(Long userId, SettingKey key);
    List<UserSetting> findByUserId(Long userId);
    boolean existsByUserIdAndKey(Long userId, SettingKey key);
}
