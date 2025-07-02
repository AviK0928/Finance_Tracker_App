package com.example.Finance_Tracker.Dashboard.controller;

import com.example.Finance_Tracker.Dashboard.dto.DashboardSummaryDTO;
import com.example.Finance_Tracker.Dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public DashboardSummaryDTO getDashboardSummary() {
        return dashboardService.getDashboardSummary();
    }
}
