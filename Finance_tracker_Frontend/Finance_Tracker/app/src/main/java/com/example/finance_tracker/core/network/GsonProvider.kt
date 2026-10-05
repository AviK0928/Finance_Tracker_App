package com.example.finance_tracker.core.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

/**
 * The single Gson instance used for the REST API.
 *
 * The backend sends and accepts java.time values as ISO-8601 strings
 * ("2026-10-05T10:00:00", "2026-10-05"). Plain Gson has no java.time support: it would try to
 * read LocalDateTime as a JSON object and fail on every response that contains a date.
 */
object GsonProvider {

    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(
            LocalDateTime::class.java,
            IsoAdapter<LocalDateTime>(DateTimeFormatter.ISO_LOCAL_DATE_TIME) { text, f -> LocalDateTime.parse(text, f) }
        )
        .registerTypeAdapter(
            LocalDate::class.java,
            IsoAdapter<LocalDate>(DateTimeFormatter.ISO_LOCAL_DATE) { text, f -> LocalDate.parse(text, f) }
        )
        .create()

    private class IsoAdapter<T : TemporalAccessor>(
        private val formatter: DateTimeFormatter,
        private val parse: (String, DateTimeFormatter) -> T
    ) : TypeAdapter<T>() {

        override fun write(out: JsonWriter, value: T?) {
            if (value == null) {
                out.nullValue()
            } else {
                out.value(formatter.format(value))
            }
        }

        override fun read(input: JsonReader): T? {
            if (input.peek() == JsonToken.NULL) {
                input.nextNull()
                return null
            }
            return parse(input.nextString(), formatter)
        }
    }
}
