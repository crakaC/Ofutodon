package com.crakac.ofutodon.db

import android.arch.persistence.room.*

@Entity(tableName = "user_tab",
        foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["uid"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)],
        indices = [Index(value = ["userId"])]
)
@TypeConverters(TabTypeConverter::class)
class UserTab {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var userId: Long = 0
    @TypeConverters(TabTypeConverter::class)
    var type: TabType = TabType.HOME
    var listId: Long = 0
    var order: Int = 0
}