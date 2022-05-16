package com.intention.android.goodmask.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.intention.android.goodmask.dao.DeviceDao
import com.intention.android.goodmask.model.DeviceData

@Database(entities = [DeviceData::class], version = 1)
abstract class DeviceDB : RoomDatabase() {
    abstract fun DeviceDao(): DeviceDao

    companion object {
        private var INSTANCE: DeviceDB? = null

        fun getInstance(context: Context): DeviceDB? {
            if (INSTANCE == null) {
                synchronized(DeviceDB::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        DeviceDB::class.java, "device.db")
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