package com.intention.android.goodmask.dao

import androidx.room.*
import com.intention.android.goodmask.model.DeviceData

@Dao
interface DeviceDao {
    @Query("SELECT * FROM device")
    fun getAll(): MutableList<DeviceData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(deviceData: DeviceData)

    @Delete
    suspend fun deleteUser(deviceData: DeviceData)

    @Update
    fun update(deviceData: DeviceData)

    @Query("SELECT deviceName FROM device WHERE deviceName = :device")
    fun checkDevice(device: String): Boolean

    @Query("DELETE FROM device WHERE deviceName = :device")
    fun deleteDevice(device: String)

    @Query("SELECT deviceName FROM device WHERE deviceName = :device")
    fun findDevice(device: String): String

    @Query("DELETE from device")
    fun deleteAll()
}