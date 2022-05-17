package com.intention.android.goodmask.dao

import androidx.room.*
import com.intention.android.goodmask.model.LocationData

@Dao
interface LocationDao {
    @Query("SELECT * FROM location")
    fun getAll(): MutableList<LocationData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(locationData: LocationData)

    @Delete
    suspend fun deleteUser(locationData: LocationData)

    @Update
    fun update(locationData: LocationData)

    @Query("DELETE from location")
    fun deleteAll()
}