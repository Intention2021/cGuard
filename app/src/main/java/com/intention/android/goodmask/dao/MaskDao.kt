package com.intention.android.goodmask.dao

import androidx.room.*
import com.intention.android.goodmask.model.MaskData
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface MaskDao {
    @Query("SELECT * FROM mask")
    fun getAll(): MutableList<MaskData>

    @Insert(onConflict = REPLACE)
    fun insert(maskData: MaskData)

    @Delete
    suspend fun deleteUser(maskData:MaskData)

    @Update
    fun update(maskData: MaskData)

    @Query(value = "SELECT time FROM mask WHERE day = :day")
    fun getTime(day: String): Long

    @Query("DELETE from mask")
    fun deleteAll()
}