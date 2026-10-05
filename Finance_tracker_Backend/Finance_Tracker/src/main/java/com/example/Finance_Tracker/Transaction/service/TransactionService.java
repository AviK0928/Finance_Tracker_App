package com.example.Finance_Tracker.Transaction.service;

import com.example.Finance_Tracker.Notification.dto.CreateNotificationDTO;
import com.example.Finance_Tracker.Notification.service.NotificationService;
import com.example.Finance_Tracker.Notification.util.NotificationType;
import com.example.Finance_Tracker.Security.SecurityUtils;
import com.example.Finance_Tracker.Transaction.dto.TransactionCreateDTO;
import com.example.Finance_Tracker.Transaction.dto.TransactionFilterDTO;
import com.example.Finance_Tracker.Transaction.dto.TransactionUpdateDTO;
import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.exception.NotFoundException;
import com.example.Finance_Tracker.Transaction.repository.TransactionRepository;
import com.example.Finance_Tracker.Transaction.util.TransactionSpecification;
import com.example.Finance_Tracker.User.entity.User;
import com.example.Finance_Tracker.User.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public List<Transaction> getFilteredTransactions(TransactionFilterDTO filter) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Specification<Transaction> spec = TransactionSpecification.filterBy(
                currentUserId,
                filter.getCategory(),
                filter.getType(),
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getMinAmount(),
                filter.getMaxAmount()
        );
        return transactionRepository.findAll(spec);
    }

    public List<Transaction> getAllTransactionsForUser(){
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return transactionRepository.findByUserId(currentUserId);
    }

    public Transaction getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + id));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!transaction.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to access this transaction");
        }
        return transaction;
    }

    public Transaction createTransaction(TransactionCreateDTO dto){
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + currentUserId));

        Transaction transaction = new Transaction();
        transaction.setUserId(currentUserId);
        transaction.setCategory(dto.getCategory());
        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setDescription(dto.getDescription());
        transaction.setTransactionDate(
                dto.getTransactionDate() != null ? dto.getTransactionDate() : LocalDateTime.now()
        );
        transaction = transactionRepository.save(transaction);

        BigDecimal amount = transaction.getAmount();
        String type = String.valueOf(transaction.getType());

        // 1️⃣ High-Value Expense (> ₹10,000)
        if ("EXPENSE".equalsIgnoreCase(type) && amount.compareTo(BigDecimal.valueOf(10000)) > 0) {
            CreateNotificationDTO dto1 = new CreateNotificationDTO();
            dto1.setTitle("High Value Expense");
            dto1.setMessage("You made a high-value expense of ₹" + amount + " in category: " + transaction.getCategory());
            dto1.setType(NotificationType.WARNING);
            dto1.setReferenceId(transaction.getId());

            notificationService.createNotification(dto1);
        }

        // 2️⃣ Category Overspend (> ₹5,000 per transaction)
        if ("EXPENSE".equalsIgnoreCase(type) && amount.compareTo(BigDecimal.valueOf(5000)) > 0) {
            String categoryTitle = "Heavy Spending in " + transaction.getCategory();

            CreateNotificationDTO dto2 = new CreateNotificationDTO();
            dto2.setTitle(categoryTitle);
            dto2.setMessage("You’ve spent over ₹5,000 in category: " + transaction.getCategory());
            dto2.setType(NotificationType.ALERT);
            dto2.setReferenceId(transaction.getId());

            notificationService.createNotification(dto2);
        }

        // 3️⃣ Frequent Transactions Today (> 5)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        List<Transaction> todayTransactions = transactionRepository.findByUserIdAndTransactionDateBetween(
                currentUserId, startOfDay, endOfDay);

        if (todayTransactions.size() > 5) {
            String title = "Frequent Transactions Today";

            boolean alreadyNotified = notificationService.existsByTitleAndDateAndUserId(
                    title, currentUserId, now.toLocalDate());

            if (!alreadyNotified) {
                CreateNotificationDTO dto3 = new CreateNotificationDTO();
                dto3.setTitle(title);
                dto3.setMessage("You’ve made " + todayTransactions.size() + " transactions today.");
                dto3.setType(NotificationType.INFO);
                dto3.setReferenceId(null);

                notificationService.createNotification(dto3);
            }
        }

        // 4️⃣ Large Income Notification (> ₹15,000)
        if ("INCOME".equalsIgnoreCase(type) && amount.compareTo(BigDecimal.valueOf(15000)) > 0) {
            CreateNotificationDTO dto4 = new CreateNotificationDTO();
            dto4.setTitle("Large Income Received");
            dto4.setMessage("You've received a large income of ₹" + amount + " in category: " + transaction.getCategory());
            dto4.setType(NotificationType.INFO);
            dto4.setReferenceId(transaction.getId());

            notificationService.createNotification(dto4);
        }

        return transaction;
    }

    public Transaction updateTransaction(Long id, TransactionUpdateDTO dto){
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + id));

        if (!transaction.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to update this transaction");
        }

        if (dto.getAmount() != null) transaction.setAmount(dto.getAmount());
        if (dto.getType() != null) transaction.setType(dto.getType());
        if (dto.getCategory() != null) transaction.setCategory(dto.getCategory());
        if (dto.getDescription() != null) transaction.setDescription(dto.getDescription());
        if (dto.getTransactionDate() != null) transaction.setTransactionDate(dto.getTransactionDate());

        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + id));

        if (!transaction.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to delete this transaction");
        }

        transactionRepository.delete(transaction);
    }

    public Page<Transaction> getTransactions(Pageable pageable, TransactionFilterDTO filter) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Specification<Transaction> spec = TransactionSpecification.filterBy(
                currentUserId,
                filter.getCategory(),
                filter.getType(),
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getMinAmount(),
                filter.getMaxAmount()
        );
        return transactionRepository.findAll(spec, pageable);
    }
}
