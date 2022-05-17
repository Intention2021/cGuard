package com.intention.android.goodmask.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location")
data class LocationData(
    @PrimaryKey var location : String
) {
    constructor(): this(
        "서울시 중구 명동"
    )
}