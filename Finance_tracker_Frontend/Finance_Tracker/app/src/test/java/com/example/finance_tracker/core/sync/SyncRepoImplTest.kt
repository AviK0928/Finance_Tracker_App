package com.example.finance_tracker.core.sync

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.finance_tracker.core.data.local.room.FinanceTrackerDatabase
import com.example.finance_tracker.core.data.local.room.entity.SyncStateEntity
import com.example.finance_tracker.core.data.local.room.mapper.BudgetMapper
import com.example.finance_tracker.core.data.local.room.mapper.TransactionMapper
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.apiendpoints.SyncApi
import com.example.finance_tracker.core.network.model.budget.BudgetFrequency
import com.example.finance_tracker.core.network.model.budget.BudgetResponseDTO
import com.example.finance_tracker.core.network.model.budget.BudgetStatus
import com.example.finance_tracker.core.network.model.sync.SyncResponseDTO
import com.example.finance_tracker.core.network.model.transaction.TransactionResponseDTO
import com.example.finance_tracker.core.network.model.transaction.TransactionType
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Sync against a real (in-memory) Room database on the JVM via Robolectric.
 * SDK 34: Robolectric's SDK 35 runtime needs Java 21, the Codespace has 17.
 * Plain Application: the Hilt application class is not needed for a database test.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class SyncRepoImplTest {

    private lateinit var database: FinanceTrackerDatabase
    private lateinit var api: FakeSyncApi
    private lateinit var repo: SyncRepoImpl

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), FinanceTrackerDatabase::class.java
        ).allowMainThreadQueries().build()
        api = FakeSyncApi()
        repo = SyncRepoImpl(api, database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun firstSync_sendsNoCursor_replacesLocalTransactions_andStoresTheCursor() = runBlocking {
        database.transactionDao().upsertAll(listOf(TransactionMapper.fromDTO(transaction(99, 5.0))))
        api.next = Response.success(SyncResponseDTO(
            cursor = "c1", fullSync = true,
            transactions = listOf(transaction(1, 10.0), transaction(2, 20.0)),
            deletedTransactionIds = emptyList(),
            budgets = listOf(budget(7, spent = 10.0))
        ))

        val result = repo.sync()

        assertTrue(result is NetworkResult.Success)
        assertEquals(listOf<String?>(null), api.cursorsSent)
        assertEquals(setOf(1L, 2L), database.transactionDao().getAll().map { it.id }.toSet())
        assertEquals(listOf(7L), database.budgetDao().getAll().map { it.id })
        assertEquals("c1", database.syncStateDao().getCursor())
    }

    @Test
    fun deltaSync_sendsTheStoredCursor_upsertsChanges_removesDeleted_andReplacesBudgets() = runBlocking {
        database.transactionDao().upsertAll(listOf(transaction(1, 10.0), transaction(2, 20.0)).map(TransactionMapper::fromDTO))
        database.budgetDao().insertAll(listOf(budget(7, spent = 10.0), budget(8, spent = 0.0)).map(BudgetMapper::fromDTO))
        database.syncStateDao().save(SyncStateEntity(cursor = "c1"))
        api.next = Response.success(SyncResponseDTO(
            cursor = "c2", fullSync = false,
            transactions = listOf(transaction(1, 15.0), transaction(3, 30.0)),
            deletedTransactionIds = listOf(2L),
            budgets = listOf(budget(8, spent = 45.0))
        ))

        repo.sync()

        assertEquals(listOf<String?>("c1"), api.cursorsSent)
        val local = database.transactionDao().getAll().associate { it.id to it.amount }
        assertEquals(mapOf(1L to 15.0, 3L to 30.0), local)
        val budgets = database.budgetDao().getAll()
        assertEquals(listOf(8L), budgets.map { it.id })
        assertEquals(45.0, budgets.single().spentAmount, 0.0)
        assertEquals("c2", database.syncStateDao().getCursor())
    }

    @Test
    fun failedSync_keepsLocalDataAndCursor() = runBlocking {
        database.transactionDao().upsertAll(listOf(TransactionMapper.fromDTO(transaction(1, 10.0))))
        database.syncStateDao().save(SyncStateEntity(cursor = "c1"))
        api.next = Response.error(500, """{"message":"boom"}""".toResponseBody("application/json".toMediaType()))

        val result = repo.sync()

        assertTrue(result is NetworkResult.Error)
        assertEquals(listOf(1L), database.transactionDao().getAll().map { it.id })
        assertEquals("c1", database.syncStateDao().getCursor())
    }

    @Test
    fun clearLocalData_removesDataAndCursor_soTheNextSyncIsFull() = runBlocking {
        database.transactionDao().upsertAll(listOf(TransactionMapper.fromDTO(transaction(1, 10.0))))
        database.budgetDao().insertAll(listOf(BudgetMapper.fromDTO(budget(7, spent = 0.0))))
        database.syncStateDao().save(SyncStateEntity(cursor = "c1"))

        repo.clearLocalData()

        assertTrue(database.transactionDao().getAll().isEmpty())
        assertTrue(database.budgetDao().getAll().isEmpty())
        assertNull(database.syncStateDao().getCursor())
    }

    /** Records the cursor of every call and answers with [next]. */
    private class FakeSyncApi : SyncApi {
        var next: Response<SyncResponseDTO>? = null
        val cursorsSent = mutableListOf<String?>()

        override suspend fun sync(cursor: String?): Response<SyncResponseDTO> {
            cursorsSent += cursor
            return next ?: error("no response prepared")
        }
    }

    private fun transaction(id: Long, amount: Double) = TransactionResponseDTO(
        id = id, userId = 1, amount = amount, category = "Food", type = TransactionType.EXPENSE,
        transactionDate = LocalDateTime.of(2026, 10, 5, 10, 0).plusMinutes(id),
        description = null,
        createdAt = LocalDateTime.of(2026, 10, 5, 10, 0),
        updatedAt = LocalDateTime.of(2026, 10, 5, 10, 0)
    )

    private fun budget(id: Long, spent: Double) = BudgetResponseDTO(
        id = id, userId = 1, name = "Budget $id", category = null, amount = 100.0,
        spentAmount = spent, remainingAmount = 100.0 - spent, percentageSpent = spent,
        startDate = LocalDate.of(2026, 10, 1), endDate = LocalDate.of(2026, 10, 31), notes = null,
        budgetFrequency = BudgetFrequency.MONTHLY, budgetStatus = BudgetStatus.ACTIVE,
        createdAt = LocalDateTime.of(2026, 10, 5, 10, 0),
        updatedAt = LocalDateTime.of(2026, 10, 5, 10, 0)
    )
}
