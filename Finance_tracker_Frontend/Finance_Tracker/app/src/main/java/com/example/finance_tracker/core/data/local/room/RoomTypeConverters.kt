package com.example.finance_tracker.core.data.local.room

import androidx.room.TypeConverter
import com.example.finance_tracker.core.network.model.settings.SettingKey
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


object RoomTypeConverters {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal): String = value.toPlainString()

    @TypeConverter
    fun toBigDecimal(value: String): BigDecimal = BigDecimal(value)

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime): String = value.format(formatter)

    @TypeConverter
    fun toLocalDateTime(value: String): LocalDateTime = LocalDateTime.parse(value, formatter)

    @TypeConverter
    fun fromSettingKey(key: SettingKey): String = key.name

    @TypeConverter
    fun toSettingKey(value: String): SettingKey = SettingKey.valueOf(value)
}