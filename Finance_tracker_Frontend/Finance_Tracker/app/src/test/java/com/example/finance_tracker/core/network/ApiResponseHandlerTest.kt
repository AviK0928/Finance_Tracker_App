package com.example.finance_tracker.core.network

import com.example.finance_tracker.core.network.model.transaction.TransactionResponseDTO
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class ApiResponseHandlerTest {

    @Test
    fun noContent_forUnitCall_isSuccess() {
        runBlocking {
            // Every DELETE returns 204: Retrofit gives a null body
            val result = ApiResponseHandler.handleApi<Unit> { Response.success<Unit>(204, null) }

            assertEquals(NetworkResult.Success(Unit), result)
        }
    }

    @Test
    fun noContent_forCallExpectingData_isError() {
        runBlocking {
            val result = ApiResponseHandler.handleApi<TransactionResponseDTO> {
                Response.success<TransactionResponseDTO>(204, null)
            }

            assertEquals(NetworkResult.Error(message = "Empty response body", code = 204), result)
        }
    }

    @Test
    fun successWithBody_isSuccess() {
        runBlocking {
            val result = ApiResponseHandler.handleApi<String> { Response.success("ok") }

            assertEquals(NetworkResult.Success("ok"), result)
        }
    }

    @Test
    fun errorResponse_usesBackendMessageAndCode() {
        runBlocking {
            val body = """{"status":400,"error":"Bad Request","message":"amount: Amount Must be Greater than 0"}"""
                .toResponseBody("application/json".toMediaType())

            val result = ApiResponseHandler.handleApi<String> { Response.error(400, body) }

            assertEquals(NetworkResult.Error(message = "amount: Amount Must be Greater than 0", code = 400), result)
        }
    }

    @Test
    fun ioException_isNetworkError() {
        runBlocking {
            val result = ApiResponseHandler.handleApi<String> { throw IOException("timeout") }

            assertTrue(result is NetworkResult.Error)
            assertEquals("Network error: timeout", (result as NetworkResult.Error).message)
        }
    }
}
