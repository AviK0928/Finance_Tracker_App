package com.example.Finance_Tracker.Transaction.controller;

import com.example.Finance_Tracker.Security.SecurityUtils;
import com.example.Finance_Tracker.Transaction.dto.TransactionCreateDTO;
import com.example.Finance_Tracker.Transaction.dto.TransactionFilterDTO;
import com.example.Finance_Tracker.Transaction.dto.TransactionResponseDTO;
import com.example.Finance_Tracker.Transaction.dto.TransactionUpdateDTO;
import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.service.TransactionService;
import com.example.Finance_Tracker.Transaction.util.PDFGenerator;
import com.example.Finance_Tracker.Transaction.util.TransactionType;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
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
    public ResponseEntity<TransactionResponseDTO> createTransaction(@Valid @RequestBody TransactionCreateDTO dto) {
        dto.setUserId(SecurityUtils.getCurrentUserId()); // Inject userId
        Transaction created = transactionService.createTransaction(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(TransactionResponseDTO.fromEntity(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getTransactionById(@PathVariable Long id) {
        Transaction transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(TransactionResponseDTO.fromEntity(transaction));
    }

    @GetMapping("/user") // Removed userId from path
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactionsForUser() {
        List<Transaction> transactions = transactionService.getAllTransactionsForUser();
        return ResponseEntity.ok(toResponses(transactions));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<TransactionResponseDTO>> getFilteredTransactions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount
    ) {
        TransactionFilterDTO filter = new TransactionFilterDTO();
        filter.setUserId(SecurityUtils.getCurrentUserId()); // Inject userId
        filter.setCategory(category);
        filter.setType(type);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setMinAmount(minAmount);
        filter.setMaxAmount(maxAmount);

        List<Transaction> filtered = transactionService.getFilteredTransactions(filter);
        return ResponseEntity.ok(toResponses(filtered));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateDTO dto) {
        Transaction updated = transactionService.updateTransaction(id, dto);
        return ResponseEntity.ok(TransactionResponseDTO.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/filter/paginated")
    public ResponseEntity<Page<TransactionResponseDTO>> getFilteredTransactionsPaginated(
            @Valid @RequestBody TransactionFilterDTO filter,
            // Spring Data resolves ?page=&size=&sort=property,direction (same parameter names as before).
            // The old String[] sort parameter was comma-split by Spring into ["transactionDate", "desc"],
            // which produced a sort on a non-existent property "desc" and a 400 on every default request.
            @PageableDefault(size = 10, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        filter.setUserId(SecurityUtils.getCurrentUserId()); // Inject userId

        Page<Transaction> pagedResult = transactionService.getTransactions(pageable, filter);
        // Page.map keeps the same page JSON shape (content, totalPages, number, ...) the Android client reads
        return ResponseEntity.ok(pagedResult.map(TransactionResponseDTO::fromEntity));
    }

    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportFilteredTransactionsToPDF(@Valid @RequestBody TransactionFilterDTO filter) {
        filter.setUserId(SecurityUtils.getCurrentUserId()); // Inject userId

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

    /** API responses never expose the JPA entity (internal fields such as contentHash stay server-side). */
    private static List<TransactionResponseDTO> toResponses(List<Transaction> transactions) {
        return transactions.stream().map(TransactionResponseDTO::fromEntity).toList();
    }
}
