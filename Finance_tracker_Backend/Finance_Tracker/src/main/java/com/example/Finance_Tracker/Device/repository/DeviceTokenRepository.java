package com.example.Finance_Tracker.Device.repository;

import com.example.Finance_Tracker.Device.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByToken(String token);

    List<DeviceToken> findByUserId(Long userId);

    /** The user's devices, most recently registered first. */
    List<DeviceToken> findByUserIdOrderByUpdatedAtDesc(Long userId);

    /** Deletes the token only if it belongs to {@code userId}; returns the number of rows deleted (0 or 1). */
    @Transactional
    @Modifying
    @Query("DELETE FROM DeviceToken d WHERE d.userId = :userId AND d.token = :token")
    int deleteByUserIdAndToken(@Param("userId") Long userId, @Param("token") String token);
}
