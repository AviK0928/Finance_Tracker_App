package com.example.Finance_Tracker.Transaction.controller;

import com.example.Finance_Tracker.Security.SecurityUtils;
import com.example.Finance_Tracker.Transaction.dto.TransactionCreateDTO;
import com.example.Finance_Tracker.Transaction.dto.TransactionFilterDTO;
import com.example.Finance_Tracker.Transaction.dto.TransactionUpdateDTO;
import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.service.TransactionService;
import com.example.Finance_Tracker.Transaction.util.PDFGenerator;
import com.example.Finance_Tracker.Transaction.util.TransactionType;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody TransactionCreateDTO dto) {
        dto.setUserId(SecurityUtils.getCurrentUserId()); // ✅ Inject userId
        Transaction created = transactionService.createTransaction(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        Transaction transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/user") // ✅ Removed userId from path
    public ResponseEntity<List<Transaction>> getAllTransactionsForUser() {
        List<Transaction> transactions = transactionService.getAllTransactionsForUser();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Transaction>> getFilteredTransactions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount
    ) {
        TransactionFilterDTO filter = new TransactionFilterDTO();
        filter.setUserId(SecurityUtils.getCurrentUserId()); // ✅ Inject userId
        filter.setCategory(category);
        filter.setType(type);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setMinAmount(minAmount);
        filter.setMaxAmount(maxAmount);

        List<Transaction> filtered = transactionService.getFilteredTransactions(filter);
        return ResponseEntity.ok(filtered);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateDTO dto) {
        Transaction updated = transactionService.updateTransaction(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/filter/paginated")
    public ResponseEntity<Page<Transaction>> getFilteredTransactionsPaginated(
            @Valid @RequestBody TransactionFilterDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate,desc") String[] sort
    ) {
        filter.setUserId(SecurityUtils.getCurrentUserId()); // ✅ Inject userId

        List<Sort.Order> orders = new ArrayList<>();
        for (String sortParam : sort) {
            String[] sortParts = sortParam.split(",");
            if (sortParts.length == 2) {
                orders.add(new Sort.Order(Sort.Direction.fromString(sortParts[1]), sortParts[0]));
            } else {
                orders.add(new Sort.Order(Sort.Direction.ASC, sortParts[0]));
            }
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
        Page<Transaction> pagedResult = transactionService.getTransactions(pageable, filter);
        return ResponseEntity.ok(pagedResult);
    }

    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportFilteredTransactionsToPDF(@Valid @RequestBody TransactionFilterDTO filter) {
        filter.setUserId(SecurityUtils.getCurrentUserId()); // ✅ Inject userId

        List<Transaction> transactions = transactionService.getFilteredTransactions(filter);

        byte[] pdfBytes = PDFGenerator.generateTransactionPDF(transactions);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition
                .attachment()
                .filename("filtered-transactions.pdf")
                .build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
