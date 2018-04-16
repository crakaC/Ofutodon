package com.crakac.ofutodon.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Delete
    fun delete(user: User)

    @Insert
    fun insert(user: User)

    @Query("SELECT * FROM user WHERE id = :id")
    fun getCurrentUser(id: Int): User
}