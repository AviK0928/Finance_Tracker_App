package com.example.finance_tracker.core.network

import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

object ApiResponseHandler {

    /**
     * Wraps a Retrofit call in a [NetworkResult].
     *
     * Inline + reified so it can tell `Response<Unit>` calls apart: a 204 No Content (every DELETE)
     * has no body, which is a success for a `Unit` call but an error for a call that expects data.
     */
    suspend inline fun <reified T : Any> handleApi(
        apiCall: suspend () -> Response<T>
    ): NetworkResult<T> {
        return try {
            val response = apiCall()

            if (response.isSuccessful) {
                val body = response.body()
                when {
                    body != null -> NetworkResult.Success(body)
                    T::class == Unit::class -> NetworkResult.Success(Unit as T)
                    else -> NetworkResult.Error(
                        message = "Empty response body",
                        code = response.code()
                    )
                }
            } else {
                val errorMsg = ErrorUtils.parseError(response)
                NetworkResult.Error(
                    message = errorMsg,
                    code = response.code()
                )
            }
        } catch (e: IOException) {
            NetworkResult.Error("Network error: ${e.localizedMessage}", null, e)
        } catch (e: CancellationException) {
            // A cancelled call (screen closed, withTimeout) must stop the caller, not become an error result
            throw e
        } catch (e: Exception) {
            NetworkResult.Error("Unexpected error: ${e.localizedMessage}", null, e)
        }
    }
}
