package com.shubham.hard75kmm.data.db.converters

import androidx.room.TypeConverter
import com.shubham.hard75kmm.data.db.entities.DayStatus

class StringListConverter {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString(",")
    }
}

class DayStatusConverter {
    @TypeConverter
    fun toDayStatus(value: String) = enumValueOf<DayStatus>(value)

    @TypeConverter
    fun fromDayStatus(value: DayStatus) = value.name
}