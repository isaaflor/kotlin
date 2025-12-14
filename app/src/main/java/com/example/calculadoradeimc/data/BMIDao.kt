package com.example.calculadoradeimc.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BMIDao {
    @Insert
    suspend fun insert(record: BMIRecord)

    @Query("SELECT * FROM bmi_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<BMIRecord>>

    // NEW: Fetch specific record
    @Query("SELECT * FROM bmi_records WHERE id = :id")
    fun getRecordById(id: Int): Flow<BMIRecord>
}