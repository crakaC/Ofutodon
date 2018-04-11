package com.crakac.ofutodon.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface UserTabDao {
    @Query(value = "SELECT * FROM user_tab WHERE userId = :userId")
    fun getUserTabs(userId: Long): List<UserTab>

    @Query(value = "DELETE FROM user_tab WHERE userId = :userId")
    fun delete(userId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(prefs: List<UserTab>)
}