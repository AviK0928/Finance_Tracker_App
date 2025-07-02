package com.example.Finance_Tracker.Budget.repository;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.util.BudgetFrequency;
import com.example.Finance_Tracker.Budget.util.BudgetStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long>, JpaSpecificationExecutor<Budget> {
    List<Budget> findByUserId(Long userId);
    List<Budget> findAllByUserIdAndUpdatedAtAfter(Long userId, LocalDateTime lastSync);
    List<Budget> findByUserIdAndStatus(Long userId, BudgetStatus status);
    @Query("SELECT MAX(b.updatedAt) FROM Budget b WHERE b.userId = :userId")
    LocalDateTime findLatestUpdateForUser(@Param("userId") Long userId);
    List<Budget> findAllByUserId(Long userId);
    boolean existsByUserIdAndContentHash(@NotNull Long userId, String contentHash);
}