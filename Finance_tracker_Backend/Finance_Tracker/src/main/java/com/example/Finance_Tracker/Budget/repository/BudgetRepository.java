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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
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

    /** One row of {@link #sumSpentPerBudget}. */
    interface BudgetSpent {
        Long getBudgetId();
        BigDecimal getSpent();
    }

    /**
     * Spending of many budgets in one query instead of one SUM per budget. Same rule as
     * {@code BudgetSpendingCalculator.spentFor}: the owner's EXPENSE transactions dated from start_date
     * to end_date inclusive, only the budget's category (case-insensitive) when it has one.
     * Every requested budget gets a row (0 when nothing matches). Aliases are quoted because
     * Postgres lower-cases unquoted ones, which would not match the projection's getter names.
     */
    @Query(value = """
            SELECT b.id AS "budgetId", COALESCE(SUM(t.amount), 0) AS "spent"
            FROM budgets b
            LEFT JOIN transactions t
              ON t.user_id = b.user_id
             AND t.type = 'EXPENSE'
             AND t.transaction_date >= b.start_date
             AND t.transaction_date < b.end_date + 1
             AND (b.category IS NULL OR LOWER(t.category) = LOWER(b.category))
            WHERE b.id IN (:budgetIds)
            GROUP BY b.id
            """, nativeQuery = true)
    List<BudgetSpent> sumSpentPerBudget(@Param("budgetIds") Collection<Long> budgetIds);
}