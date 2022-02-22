package com.intention.android.goodmask.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.intention.android.goodmask.dao.MaskDao
import com.intention.android.goodmask.model.MaskData

@Database(entities = [MaskData::class], version = 1)
abstract class MaskDB: RoomDatabase() {
    abstract fun MaskDao(): MaskDao

    companion object {
        private var INSTANCE: MaskDB? = null

        fun getInstance(context: Context): MaskDB? {
            if (INSTANCE == null) {
                synchronized(MaskDB::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        MaskDB::class.java, "mask.db")
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
