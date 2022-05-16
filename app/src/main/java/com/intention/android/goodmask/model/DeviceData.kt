package com.intention.android.goodmask.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device")
data class DeviceData(
    @PrimaryKey var deviceName: String
){
    constructor() : this(
        "Mask"
    )
}