package com.crakac.ofutodon.db

import android.arch.persistence.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Delete
    fun delete(user: User)

    @Insert
    fun insert(user: User)

    @Query("SELECT * FROM user WHERE id = :id")
    fun getUser(id: Int): User

    @Query("SELECT * FROM user WHERE userId = :userId AND domain = :domain")
    fun select(userId: Long, domain: String): User

    @Update
    fun update(user: User)
}