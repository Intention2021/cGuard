package com.intention.android.goodmask.model

import android.text.format.Time
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(tableName = "mask")
class MaskData(@PrimaryKey var address: String,
          @ColumnInfo(name = "name") var name: String,
               @ColumnInfo(name = "fan") var fan: Int,
               @ColumnInfo(name = "end") var end : String,
               @ColumnInfo(name = "start") var start : String,
){
    constructor(): this("","", 0, Timestamp(System.currentTimeMillis()).toString(), Timestamp(System.currentTimeMillis()).toString())
}