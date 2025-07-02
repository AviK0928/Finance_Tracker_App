package com.example.Finance_Tracker.Report.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Report {
    private LocalDateTime date;
    private BigDecimal income;
    private BigDecimal expense;
}
