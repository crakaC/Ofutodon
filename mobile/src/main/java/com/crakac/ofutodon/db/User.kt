package com.crakac.ofutodon.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "user")
class User {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    var name: String = ""
    var domain: String = ""
    var token: String = ""
}