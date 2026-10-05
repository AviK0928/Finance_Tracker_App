package com.example.Finance_Tracker.Transaction.repository;

import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.util.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {
        List<Transaction> findByUserId(Long userId);

        /** The user's newest transactions (dashboard), without loading the rest. */
        List<Transaction> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

        /** One row of {@link #sumAmountByType}. */
        interface TypeTotal {
                TransactionType getTransactionType();
                BigDecimal getTotal();
        }

        /** All-time totals per type for one user (dashboard), computed in the database. */
        @Query("SELECT t.type AS transactionType, SUM(t.amount) AS total FROM Transaction t WHERE t.userId = :userId GROUP BY t.type")
        List<TypeTotal> sumAmountByType(@Param("userId") Long userId);
        Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);
        List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDateTime start, LocalDateTime end);
        List<Transaction> findAllByUserIdAndUpdatedAtAfter(Long userId, LocalDateTime lastSync);
        @Query("SELECT MAX(t.updatedAt) FROM Transaction t WHERE t.userId = :userId")
        LocalDateTime findLatestUpdateForUser(@Param("userId") Long userId);
        List<Transaction> findAllByUserId(Long userId);
        boolean existsByUserIdAndContentHash(Long userId, String contentHash);

        /** Categories the user has used, for category suggestions in the app. */
        @Query("SELECT DISTINCT t.category FROM Transaction t WHERE t.userId = :userId ORDER BY t.category")
        List<String> findDistinctCategoriesByUserId(@Param("userId") Long userId);

        /** Total of the user's transactions of one type in [from, to). Null when there are none. */
        @Query("""
                SELECT SUM(t.amount) FROM Transaction t
                WHERE t.userId = :userId AND t.type = :type
                  AND t.transactionDate >= :from AND t.transactionDate < :to
                """)
        BigDecimal sumAmount(@Param("userId") Long userId,
                             @Param("type") TransactionType type,
                             @Param("from") LocalDateTime from,
                             @Param("to") LocalDateTime to);

        /** Same as {@link #sumAmount}, restricted to one category (case-insensitive). */
        @Query("""
                SELECT SUM(t.amount) FROM Transaction t
                WHERE t.userId = :userId AND t.type = :type
                  AND LOWER(t.category) = LOWER(:category)
                  AND t.transactionDate >= :from AND t.transactionDate < :to
                """)
        BigDecimal sumAmountForCategory(@Param("userId") Long userId,
                                        @Param("type") TransactionType type,
                                        @Param("category") String category,
                                        @Param("from") LocalDateTime from,
                                        @Param("to") LocalDateTime to);
}