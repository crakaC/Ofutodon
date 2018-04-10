package com.crakac.ofutodon.db

import android.arch.persistence.room.TypeConverter

class TabTypeConverter {
    @TypeConverter
    fun toInt(type: TabType): Int = type.value

    @TypeConverter
    fun toTabType(rawValue: Int): TabType = TabType.values().first { e -> e.value == rawValue }
}