package com.example.Finance_Tracker.Report.controller;

import com.example.Finance_Tracker.Security.*;
import com.example.Finance_Tracker.Report.dto.CategoryReportDTO;
import com.example.Finance_Tracker.Report.dto.MonthlyReportDTO;
import com.example.Finance_Tracker.Report.dto.TrendReportDTO;
import com.example.Finance_Tracker.Report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Month;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/monthly")
    public MonthlyReportDTO getMonthlyReport(
            @RequestParam Month month,
            @RequestParam int year) {
        return reportService.getMonthlyReport(month, year);
    }

    @GetMapping("/category")
    public List<CategoryReportDTO> getCategoryReport() {
        return reportService.getCategoryWiseReport();
    }

    @GetMapping("/trend")
    public List<TrendReportDTO> getTrendReport(
            @RequestParam(defaultValue = "6m") String period) {
        return reportService.getTrendReport(period);
    }
}

