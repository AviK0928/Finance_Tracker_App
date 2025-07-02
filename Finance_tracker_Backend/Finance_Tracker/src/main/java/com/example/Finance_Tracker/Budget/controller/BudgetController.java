package com.example.Finance_Tracker.Budget.controller;

import com.example.Finance_Tracker.Budget.dto.BudgetCreateDTO;
import com.example.Finance_Tracker.Budget.dto.BudgetFilterDTO;
import com.example.Finance_Tracker.Budget.dto.BudgetResponseDTO;
import com.example.Finance_Tracker.Budget.dto.BudgetUpdateDTO;
import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.service.BudgetService;
import com.example.Finance_Tracker.Budget.util.BudgetFrequency;
import com.example.Finance_Tracker.Budget.util.BudgetStatus;
import com.example.Finance_Tracker.Budget.util.PDFGenerator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponseDTO> createBudget(@Valid @RequestBody BudgetCreateDTO dto) {
        BudgetResponseDTO created = budgetService.createBudget(dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponseDTO> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetUpdateDTO dto) {
        BudgetResponseDTO updated = budgetService.updateBudget(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponseDTO> getBudgetById(@PathVariable Long id) {
        BudgetResponseDTO budget = budgetService.getBudgetById(id);
        return ResponseEntity.ok(budget);
    }

    @GetMapping("/user")
    public ResponseEntity<List<BudgetResponseDTO>> getBudgetsByUser() {
        List<BudgetResponseDTO> budgets = budgetService.getBudgetsByUser();
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportBudgetsAsPdf(
            @RequestParam(required = false) BudgetStatus status,
            @RequestParam(required = false) BudgetFrequency frequency) {

        BudgetFilterDTO filter = new BudgetFilterDTO();
        filter.setStatus(status);
        filter.setFrequency(frequency);

        // userId will be set inside service via SecurityUtils
        List<Budget> budgets = budgetService.getFilteredBudgets(filter);
        byte[] pdfBytes = PDFGenerator.generateBudgetPDF(budgets);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "budgets.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
