package com.intention.android.goodmask.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(tableName = "mask")
class MaskData(@PrimaryKey var address: String,
          @ColumnInfo(name = "name") var name: String,
){
    constructor(): this("","")
}