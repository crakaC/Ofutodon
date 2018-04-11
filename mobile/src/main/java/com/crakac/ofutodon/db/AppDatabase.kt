package com.crakac.ofutodon.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = [User::class, UserTab::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        val DATABASE_NAME = "ofutodon.db"
        var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                synchronized(AppDatabase::class) {
                    if (instance == null) {
                        instance = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
                    }
                }
            }
            return instance!!
        }
    }

    abstract fun userDao(): UserDao
    abstract fun tabPrefsDao(): UserTabDao
}