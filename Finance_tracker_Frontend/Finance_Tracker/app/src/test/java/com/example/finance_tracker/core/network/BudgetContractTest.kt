package com.example.finance_tracker.core.network

import com.example.finance_tracker.core.network.model.budget.BudgetCreateDTO
import com.example.finance_tracker.core.network.model.budget.BudgetFrequency
import com.example.finance_tracker.core.network.model.budget.BudgetResponseDTO
import com.example.finance_tracker.core.network.model.budget.BudgetStatus
import com.example.finance_tracker.core.network.model.budget.BudgetUpdateDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class BudgetContractTest {

    private val gson = GsonProvider.gson

    @Test
    fun parsesBudgetJsonAsSentByBackend() {
        // Shape of backend BudgetResponseDTO (field names and types as Jackson writes them)
        val json = """{"id":7,"userId":37,"name":"Groceries","category":"Food","amount":5000.00,
            "spentAmount":1250.50,"remainingAmount":3749.50,"percentageSpent":25.01,
            "startDate":"2026-10-01","endDate":"2026-10-31","notes":null,
            "budgetFrequency":"MONTHLY","budgetStatus":"ACTIVE",
            "createdAt":"2026-10-05T05:09:33.21051888","updatedAt":"2026-10-05T05:09:33.21051888"}"""

        val dto = gson.fromJson(json, BudgetResponseDTO::class.java)

        assertEquals(7L, dto.id)
        assertEquals("Groceries", dto.name)
        assertEquals("Food", dto.category)
        assertEquals(5000.0, dto.amount, 0.0)
        assertEquals(1250.5, dto.spentAmount, 0.0)
        assertEquals(3749.5, dto.remainingAmount, 0.0)
        assertEquals(25.01, dto.percentageSpent, 0.0)
        assertEquals(LocalDate.of(2026, 10, 1), dto.startDate)
        assertEquals(LocalDate.of(2026, 10, 31), dto.endDate)
        assertNull(dto.notes)
        assertEquals(BudgetFrequency.MONTHLY, dto.budgetFrequency)
        assertEquals(BudgetStatus.ACTIVE, dto.budgetStatus)
        assertEquals(LocalDateTime.of(2026, 10, 5, 5, 9, 33, 210_518_880), dto.createdAt)
    }

    @Test
    fun parsesBackendOnlyEnumValuesAndNullCategory() {
        // NONE and PAUSED did not exist in the old Android enums; Gson would have produced null
        val json = """{"id":8,"userId":37,"name":"Anything","category":null,"amount":100,
            "spentAmount":0,"remainingAmount":100,"percentageSpent":0.0,
            "startDate":"2026-10-01","endDate":"2026-10-01","notes":"n",
            "budgetFrequency":"NONE","budgetStatus":"PAUSED",
            "createdAt":"2026-10-05T10:00:00","updatedAt":"2026-10-05T10:00:00"}"""

        val dto = gson.fromJson(json, BudgetResponseDTO::class.java)

        assertNull(dto.category)
        assertEquals(BudgetFrequency.NONE, dto.budgetFrequency)
        assertEquals(BudgetStatus.PAUSED, dto.budgetStatus)
    }

    @Test
    fun createRequest_usesBackendFieldNamesAndPlainDates() {
        val dto = BudgetCreateDTO(
            name = "Groceries",
            category = null,
            amount = 5000.0,
            startDate = LocalDate.of(2026, 10, 1),
            endDate = LocalDate.of(2026, 10, 31),
            frequency = BudgetFrequency.MONTHLY
        )

        val json = gson.toJson(dto)

        assertTrue(json, json.contains("\"name\":\"Groceries\""))
        assertTrue(json, json.contains("\"startDate\":\"2026-10-01\""))
        assertTrue(json, json.contains("\"endDate\":\"2026-10-31\""))
        assertTrue(json, json.contains("\"frequency\":\"MONTHLY\""))
        assertFalse(json, json.contains("title"))
        assertFalse(json, json.contains("T00:00"))
    }

    @Test
    fun updateRequest_carriesStatus() {
        val dto = BudgetUpdateDTO(
            name = "Groceries",
            category = "Food",
            amount = 6000.0,
            startDate = LocalDate.of(2026, 10, 1),
            endDate = LocalDate.of(2026, 10, 31),
            frequency = BudgetFrequency.MONTHLY,
            status = BudgetStatus.COMPLETED
        )

        val json = gson.toJson(dto)

        assertTrue(json, json.contains("\"status\":\"COMPLETED\""))
        assertTrue(json, json.contains("\"category\":\"Food\""))
    }
}
