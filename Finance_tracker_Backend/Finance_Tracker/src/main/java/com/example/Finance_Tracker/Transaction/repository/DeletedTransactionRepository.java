package com.example.Finance_Tracker.Transaction.repository;

import com.example.Finance_Tracker.Transaction.entity.DeletedTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DeletedTransactionRepository extends JpaRepository<DeletedTransaction, Long> {

    /** Ids of the user's transactions deleted after {@code since} (delta sync). */
    @Query("SELECT d.transactionId FROM DeletedTransaction d WHERE d.userId = :userId AND d.deletedAt > :since")
    List<Long> findTransactionIdsDeletedSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
