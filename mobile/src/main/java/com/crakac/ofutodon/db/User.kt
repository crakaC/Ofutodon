package com.crakac.ofutodon.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "user")
data class User(
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,
        var userId: Long = 0,
        var avator: String = "",
        var name: String = "",
        var displayName: String = "",
        var domain: String = "",
        var token: String = ""
)