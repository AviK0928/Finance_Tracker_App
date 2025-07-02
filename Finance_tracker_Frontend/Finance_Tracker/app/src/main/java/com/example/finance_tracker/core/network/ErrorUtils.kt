package com.example.finance_tracker.core.network

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException

object ErrorUtils {

    private val gson = Gson()

    data class ErrorResponse(
        val status: String? = null,
        val message: String? = null
    )

    fun parseErrorMessage(errorBody: ResponseBody?): String {
        return try {
            val error = gson.fromJson(errorBody?.string(), ErrorResponse::class.java)
            error.message ?: "Unknown error"
        } catch (e: IOException) {
            "Unable to read error message"
        } catch (e: JsonSyntaxException) {
            "Invalid error response format"
        }
    }

    fun <T> parseError(response: Response<T>): String {
        return parseErrorMessage(response.errorBody())
    }
}

