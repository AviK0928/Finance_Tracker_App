package com.example.Finance_Tracker.Transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Record of a deleted transaction, so a delta sync can tell the app to drop its local copy. */
@Entity
@Table(name = "deleted_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeletedTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long transactionId;

    @Column(nullable = false)
    private LocalDateTime deletedAt;

    public DeletedTransaction(Long userId, Long transactionId, LocalDateTime deletedAt) {
        this.userId = userId;
        this.transactionId = transactionId;
        this.deletedAt = deletedAt;
    }
}
