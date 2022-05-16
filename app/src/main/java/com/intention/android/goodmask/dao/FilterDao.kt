package com.intention.android.goodmask.dao

import androidx.room.*
import com.intention.android.goodmask.model.FilterDateData

@Dao
interface FilterDao {
    @Query("SELECT * FROM filter")
    fun getAll(): MutableList<FilterDateData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(filterDateData: FilterDateData)

    @Delete
    suspend fun deleteUser(filterDateData: FilterDateData)

    @Update
    fun update(filterDateData: FilterDateData)

    @Query("SELECT date from filter")
    fun getTime(): Long

    @Query("DELETE from filter")
    fun deleteAll()
}