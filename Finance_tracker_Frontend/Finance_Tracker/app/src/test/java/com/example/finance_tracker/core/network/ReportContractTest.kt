package com.example.finance_tracker.core.network

import com.example.finance_tracker.core.network.model.dashboard.BudgetInfo
import com.example.finance_tracker.core.network.model.report.MonthlyReportDTO
import com.example.finance_tracker.core.network.model.report.TrendReportDTO
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/** Shapes of the backend report and dashboard DTOs (field names as Jackson writes them). */
class ReportContractTest {

    private val gson = GsonProvider.gson

    @Test
    fun monthlyReport_parsesBackendFields() {
        val json = """{"year":2026,"month":"OCTOBER","totalIncome":20000.00,"totalExpense":350.50,
            "netSavings":19649.50,"categoryBreakdown":{"Food":350.50}}"""

        val dto = gson.fromJson(json, MonthlyReportDTO::class.java)

        assertEquals("OCTOBER", dto.month)
        assertEquals(20000.0, dto.totalIncome, 0.0)
        assertEquals(350.5, dto.totalExpense, 0.0)
        assertEquals(19649.5, dto.netSavings, 0.0)
        assertEquals(350.5, dto.categoryBreakdown.getValue("Food"), 0.0)
    }

    @Test
    fun trendReport_parsesBackendFields() {
        val json = """[{"date":"2026-10-05T00:00:00","income":1000.00,"expense":350.50}]"""
        val type = object : TypeToken<List<TrendReportDTO>>() {}.type

        val list: List<TrendReportDTO> = gson.fromJson(json, type)

        assertEquals(LocalDateTime.of(2026, 10, 5, 0, 0), list[0].date)
        assertEquals(1000.0, list[0].income, 0.0)
        assertEquals(350.5, list[0].expense, 0.0)
    }

    @Test
    fun dashboardBudgetInfo_hasOnlyThePerBudgetList() {
        val dto = gson.fromJson("""{"activeBudgets":[]}""", BudgetInfo::class.java)

        assertTrue(dto.activeBudgets.isEmpty())
    }
}
