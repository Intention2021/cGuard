package com.intention.android.goodmask.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.intention.android.goodmask.dao.FilterDao
import com.intention.android.goodmask.model.FilterDateData

@Database(entities = [FilterDateData::class], version = 2)
abstract class FilterDB: RoomDatabase() {
    abstract fun FilterDao(): FilterDao

    companion object {
        private var INSTANCE: FilterDB? = null

        fun getInstance(context: Context): FilterDB? {
            if (INSTANCE == null) {
                synchronized(FilterDB::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        FilterDB::class.java, "filter.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}