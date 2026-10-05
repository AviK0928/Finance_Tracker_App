package com.example.finance_tracker.core.network

import com.example.finance_tracker.core.network.model.sync.SyncResponseDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SyncContractTest {

    @Test
    fun parsesSyncJsonAsSentByBackend() {
        // Shape of backend Sync/dto/SyncResponseDTO (a Java record, written by Jackson)
        val json = """{"cursor":"2026-10-05T15:16:30.655138087","fullSync":false,
            "transactions":[{"id":621,"userId":5,"description":"9b edited","amount":120.00,"type":"EXPENSE",
              "transactionDate":"2026-10-05T15:16:29","createdAt":"2026-10-05T15:16:29.1","updatedAt":"2026-10-05T15:16:31.2",
              "category":"Food"}],
            "deletedTransactionIds":[620],
            "budgets":[{"id":7,"userId":5,"name":"Food 9b","category":"Food","amount":500.00,
              "spentAmount":120.00,"remainingAmount":380.00,"percentageSpent":24.0,
              "startDate":"2026-10-01","endDate":"2026-10-31","notes":null,
              "budgetFrequency":"MONTHLY","budgetStatus":"ACTIVE",
              "createdAt":"2026-10-05T15:16:29","updatedAt":"2026-10-05T15:16:29"}]}"""

        val dto = GsonProvider.gson.fromJson(json, SyncResponseDTO::class.java)

        assertEquals("2026-10-05T15:16:30.655138087", dto.cursor)
        assertFalse(dto.fullSync)
        assertEquals(621L, dto.transactions.single().id)
        assertEquals(120.0, dto.transactions.single().amount, 0.0)
        assertEquals(listOf(620L), dto.deletedTransactionIds)
        assertEquals(120.0, dto.budgets.single().spentAmount, 0.0)
    }
}
