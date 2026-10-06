package com.example.finance_tracker.core.network

import com.example.finance_tracker.core.network.model.dashboard.BudgetInfo
import com.example.finance_tracker.core.network.model.report.MonthlyReportDTO
import com.example.finance_tracker.core.network.model.report.TrendReportDTO
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
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
        assertEquals(BigDecimal("20000.00"), dto.totalIncome)
        assertEquals(BigDecimal("350.50"), dto.totalExpense)
        assertEquals(BigDecimal("19649.50"), dto.netSavings)
        assertEquals(BigDecimal("350.50"), dto.categoryBreakdown.getValue("Food"))
    }

    @Test
    fun trendReport_parsesBackendFields() {
        val json = """[{"date":"2026-10-05T00:00:00","income":1000.00,"expense":350.50}]"""
        val type = object : TypeToken<List<TrendReportDTO>>() {}.type

        val list: List<TrendReportDTO> = gson.fromJson(json, type)

        assertEquals(LocalDateTime.of(2026, 10, 5, 0, 0), list[0].date)
        assertEquals(BigDecimal("1000.00"), list[0].income)
        assertEquals(BigDecimal("350.50"), list[0].expense)
    }

    @Test
    fun dashboardBudgetInfo_hasOnlyThePerBudgetList() {
        val dto = gson.fromJson("""{"activeBudgets":[]}""", BudgetInfo::class.java)

        assertTrue(dto.activeBudgets.isEmpty())
    }
}
