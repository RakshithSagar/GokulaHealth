package com.example.gokulahealth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gokulahealth.data.Cattle
import com.example.gokulahealth.data.GokulaDatabase
import com.example.gokulahealth.data.GokulaRepository
import com.example.gokulahealth.data.MilkRecord
import com.example.gokulahealth.data.Vaccination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map // CRITICAL: Fixes the 'Unresolved reference' error
import kotlinx.coroutines.launch

class CattleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GokulaRepository

    init {
        val cattleDao = GokulaDatabase.getDatabase(application).gokulaDao()
        repository = GokulaRepository(cattleDao)
    }

    // --- Cattle Management ---
    fun insertCattle(cattle: Cattle) = viewModelScope.launch {
        repository.insertCattle(cattle)
    }

    fun getAllCattle(): Flow<List<Cattle>> {
        return repository.getAllCattle()
    }

    fun deleteCattle(cattle: Cattle) = viewModelScope.launch {
        repository.deleteCattle(cattle)
    }

    // --- Milk Diary Management ---
    fun insertMilkRecord(record: MilkRecord) = viewModelScope.launch {
        repository.insertMilkRecord(record)
    }

    fun getMilkRecords(cattleId: Int): Flow<List<MilkRecord>> {
        return repository.getMilkRecords(cattleId)
    }

    // --- Vaccination Management (FR-03 & FR-04) ---

    /**
     * Saves a vaccination schedule.
     * Mandatory for providing digital health records.
     */
    fun insertVaccination(vaccination: Vaccination) = viewModelScope.launch {
        repository.insertVaccination(vaccination)
    }

    /**
     * Retrieves all scheduled vaccinations for a specific animal.
     */
    fun getVaccinations(cattleId: Int): Flow<List<Vaccination>> {
        return repository.getVaccinationsForCattle(cattleId)
    }

    /**
     * Removes a vaccination record from the local database.
     */
    fun deleteVaccination(vaccination: Vaccination) = viewModelScope.launch {
        repository.deleteVaccination(vaccination)
    }

    /**
     * SUCCESS CRITERIA: Automatically calculate the "Monthly Average Yield".
     * This uses a local database query to ensure it works offline.
     */
    fun getMonthlyAverage(cattleId: Int): Flow<Double> {
        return repository.getMilkRecords(cattleId).map { records ->
            if (records.isEmpty()) {
                0.0
            } else {
                // Summing all morning and evening logs safely
                val totalYield = records.sumOf { it.morningYield + it.eveningYield }
                totalYield / records.size
            }
        }
    }
}