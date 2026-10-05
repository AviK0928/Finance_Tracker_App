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

import java.time.LocalDate;
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

    /** Budgets an expense on {@code date} in {@code category} counts towards (category NULL = all categories). */
    @Query("""
            SELECT b FROM Budget b
            WHERE b.userId = :userId AND b.status = :status
              AND b.startDate <= :date AND b.endDate >= :date
              AND (b.category IS NULL OR LOWER(b.category) = LOWER(:category))
            """)
    List<Budget> findBudgetsCovering(@Param("userId") Long userId,
                                     @Param("status") BudgetStatus status,
                                     @Param("date") LocalDate date,
                                     @Param("category") String category);

    /** Ended before {@code date} and not yet announced as expired (used by the daily scheduler). */
    List<Budget> findByStatusAndExpiryNotificationSentFalseAndEndDateBefore(BudgetStatus status, LocalDate date);

    /** Ending exactly on {@code date} and not yet announced as nearing expiry (used by the daily scheduler). */
    List<Budget> findByStatusAndNearingExpiryNotificationSentFalseAndEndDate(BudgetStatus status, LocalDate date);
}