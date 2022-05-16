package com.intention.android.goodmask.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mask")
data class MaskData(
    @PrimaryKey var day: String,
    @ColumnInfo(name = "start") var start: Long,
    @ColumnInfo(name = "end") var end: Long,
    @ColumnInfo(name = "time") var time: Long,
    @ColumnInfo(name = "total") var total: Long
) {
    constructor() : this(
        "1",
        System.currentTimeMillis(),
        0,
        0,
        0
    )
}