package com.example.finance_tracker.core.network

import retrofit2.Response
import java.io.IOException

object ApiResponseHandler {

    suspend fun <T> handleApi(
        apiCall: suspend () -> Response<T>
    ): NetworkResult<T> {
        return try {
            val response = apiCall()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    NetworkResult.Success(body)
                } else {
                    NetworkResult.Error(
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
        } catch (e: Exception) {
            NetworkResult.Error("Unexpected error: ${e.localizedMessage}", null, e)
        }
    }
}