package com.intention.android.goodmask.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
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

    @Query("DELETE from mask")
    fun deleteAll()

    @Query("SELECT * FROM mask WHERE name LIKE :searchName")
    fun findMaskWithName(searchName : String) : MaskData
}