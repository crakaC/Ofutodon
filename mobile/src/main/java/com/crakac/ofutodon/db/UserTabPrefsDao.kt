package com.crakac.ofutodon.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface UserTabPrefsDao {
    @Query(value = "SELECT * FROM user_tab_prefs WHERE userId = :userId")
    fun getUserTabPrefs(userId: Long): List<UserTabPrefs>

    @Query(value = "DELETE FROM user_tab_prefs WHERE userId = :userId")
    fun delete(userId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(prefs: List<UserTabPrefs>)
}