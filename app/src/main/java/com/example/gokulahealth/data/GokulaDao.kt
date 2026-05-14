package com.example.gokulahealth.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GokulaDao {

    // --- Cattle Profile Management ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCattle(cattle: Cattle)

    @Query("SELECT * FROM cattle_table ORDER BY id ASC")
    fun getAllCattle(): Flow<List<Cattle>>

    @Delete
    suspend fun deleteCattle(cattle: Cattle)

    // --- Milk Diary Management ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilkRecord(record: MilkRecord)

    @Query("SELECT * FROM milk_diary_table WHERE cattleId = :cattleId ORDER BY date DESC LIMIT 30")
    fun getMilkRecordsForCattle(cattleId: Int): Flow<List<MilkRecord>>

    // --- NEW: Vaccination Alert System (FR-03 & FR-04) ---

    // Allows farmers to create and edit upcoming vaccination events
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccination(vaccination: Vaccination)

    // Retrieves scheduled shots sorted by the nearest due date
    @Query("SELECT * FROM vaccinations WHERE cattleId = :cattleId ORDER BY dueDate ASC")
    fun getVaccinationsForCattle(cattleId: Int): Flow<List<Vaccination>>

    // Allows removing records once completed or no longer needed
    @Delete
    suspend fun deleteVaccination(vaccination: Vaccination)
}