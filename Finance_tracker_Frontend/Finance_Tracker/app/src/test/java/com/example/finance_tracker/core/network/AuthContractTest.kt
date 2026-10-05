package com.example.finance_tracker.core.network

import com.example.finance_tracker.core.network.model.auth.MessageResponseDTO
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

class AuthContractTest {

    // Same converter setup as RetrofitInstance
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://localhost/")
        .addConverterFactory(GsonConverterFactory.create(GsonProvider.gson))
        .build()

    private fun <T> convert(type: Type, body: String, mediaType: String): T? {
        val converter = retrofit.responseBodyConverter<T>(type, emptyArray())
        return converter.convert(body.toResponseBody(mediaType.toMediaType()))
    }

    @Test
    fun plainTextBody_cannotBeReadAsString_byTheGsonConverter() {
        // What the backend used to send for forgot/reset password. With only the Gson converter,
        // Response<String> treats it as JSON and fails, so the app showed an error on success.
        assertThrows(Exception::class.java) {
            convert<String>(String::class.java, "Password has been successfully reset.", "text/plain")
        }
    }

    @Test
    fun messageJson_parsesIntoMessageResponseDTO() {
        val dto = convert<MessageResponseDTO>(
            MessageResponseDTO::class.java,
            """{"message":"Password has been successfully reset."}""",
            "application/json"
        )

        assertEquals("Password has been successfully reset.", dto?.message)
    }
}
