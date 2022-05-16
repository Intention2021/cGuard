package com.intention.android.goodmask.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "filter")
data class FilterDateData(
    @PrimaryKey var date: Long
) {
    constructor(): this(
        System.currentTimeMillis()
    )
}