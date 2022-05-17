package com.intention.android.goodmask.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.intention.android.goodmask.dao.LocationDao
import com.intention.android.goodmask.model.LocationData

@Database(entities = [LocationData::class], version = 1)
abstract class LocationDB: RoomDatabase() {
    abstract fun LocationDao(): LocationDao

    companion object {
        private var INSTANCE: LocationDB? = null

        fun getInstance(context: Context): LocationDB? {
            if (INSTANCE == null) {
                synchronized(LocationDB::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        LocationDB::class.java, "location.db")
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