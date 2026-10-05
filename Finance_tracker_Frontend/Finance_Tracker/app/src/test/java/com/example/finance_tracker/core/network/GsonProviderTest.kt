package com.example.finance_tracker.core.network

import com.example.finance_tracker.core.network.model.transaction.TransactionCreateDTO
import com.example.finance_tracker.core.network.model.transaction.TransactionResponseDTO
import com.example.finance_tracker.core.network.model.transaction.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class GsonProviderTest {

    private val gson = GsonProvider.gson

    @Test
    fun parsesTransactionJsonAsSentByBackend() {
        // Body captured from POST /api/transactions on the running backend
        val json = """{"id":118,"userId":37,"description":"Lunch","amount":250.75,"type":"EXPENSE",
            "transactionDate":"2026-10-05T10:00:00","createdAt":"2026-10-05T05:09:33.21051888",
            "updatedAt":"2026-10-05T05:09:33.21051888","category":"Food"}"""

        val dto = gson.fromJson(json, TransactionResponseDTO::class.java)

        assertEquals(118L, dto.id)
        assertEquals(250.75, dto.amount, 0.0)
        assertEquals(TransactionType.EXPENSE, dto.type)
        assertEquals(LocalDateTime.of(2026, 10, 5, 10, 0), dto.transactionDate)
        assertEquals(LocalDateTime.of(2026, 10, 5, 5, 9, 33, 210_518_880), dto.createdAt)
        assertEquals("Lunch", dto.description)
    }

    @Test
    fun missingDescription_isNull() {
        val json = """{"id":1,"userId":1,"amount":10,"type":"INCOME","category":"Salary",
            "transactionDate":"2026-10-05T10:00:00","createdAt":"2026-10-05T10:00:00",
            "updatedAt":"2026-10-05T10:00:00","description":null}"""

        val dto = gson.fromJson(json, TransactionResponseDTO::class.java)

        assertNull(dto.description)
    }

    @Test
    fun serializesRequestDatesAsIsoStrings() {
        val dto = TransactionCreateDTO(
            amount = 250.75,
            category = "Food",
            type = TransactionType.EXPENSE,
            transactionDate = LocalDateTime.of(2026, 10, 5, 10, 0),
            description = "Lunch"
        )

        val json = gson.toJson(dto)

        assertTrue(json, json.contains("\"transactionDate\":\"2026-10-05T10:00:00\""))
    }

    @Test
    fun localDate_roundTripsAsIsoDate() {
        assertEquals("\"2026-10-31\"", gson.toJson(LocalDate.of(2026, 10, 31)))
        assertEquals(LocalDate.of(2026, 10, 31), gson.fromJson("\"2026-10-31\"", LocalDate::class.java))
    }
}
