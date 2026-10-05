package com.example.finance_tracker.core.network

import com.example.finance_tracker.core.network.model.settings.ImportSummaryDTO
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

class SettingsContractTest {

    // Same converter setup as RetrofitInstance
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://localhost/")
        .addConverterFactory(GsonConverterFactory.create(GsonProvider.gson))
        .build()

    // First bytes of a real ZIP ("PK\u0003\u0004") followed by binary data
    private val zipBytes = byteArrayOf(0x50, 0x4B, 0x03, 0x04, 0x14, 0x00, 0x08, 0x00, 0x00, 0x7F)

    private fun <T> convert(type: Type, body: ResponseBody): T? =
        retrofit.responseBodyConverter<T>(type, emptyArray()).convert(body)

    @Test
    fun zipBody_cannotBeReadAsByteArray_byTheGsonConverter() {
        // What SettingsApi.exportData used to declare: Gson tries to read the ZIP as a JSON array
        assertThrows(Exception::class.java) {
            convert<ByteArray>(ByteArray::class.java, zipBytes.toResponseBody("application/octet-stream".toMediaType()))
        }
    }

    @Test
    fun zipBody_asResponseBody_keepsTheRawBytes() {
        val body = convert<ResponseBody>(
            ResponseBody::class.java,
            zipBytes.toResponseBody("application/octet-stream".toMediaType())
        )

        assertArrayEquals(zipBytes, body?.bytes())
    }

    @Test
    fun importSummary_parsesBackendJson() {
        val json = """{"budgetsImported":2,"budgetsSkipped":1,"transactionsImported":5,
            "transactionsSkipped":0,"settingsImported":1,"settingsSkipped":3}"""

        val dto = GsonProvider.gson.fromJson(json, ImportSummaryDTO::class.java)

        assertEquals(2, dto.budgetsImported)
        assertEquals(5, dto.transactionsImported)
        assertEquals(3, dto.settingsSkipped)
    }
}
