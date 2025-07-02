package com.example.Finance_Tracker.Transaction.repository;

import com.example.Finance_Tracker.Transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {
        List<Transaction> findByUserId(Long userId);
        Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);
        List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDateTime start, LocalDateTime end);
        List<Transaction> findAllByUserIdAndUpdatedAtAfter(Long userId, LocalDateTime lastSync);
        @Query("SELECT MAX(t.updatedAt) FROM Transaction t WHERE t.userId = :userId")
        LocalDateTime findLatestUpdateForUser(@Param("userId") Long userId);
        List<Transaction> findAllByUserId(Long userId);
        boolean existsByUserIdAndContentHash(Long userId, String contentHash);
}