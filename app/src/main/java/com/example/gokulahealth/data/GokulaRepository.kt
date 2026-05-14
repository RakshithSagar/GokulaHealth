package com.example.gokulahealth.data

import kotlinx.coroutines.flow.Flow

class GokulaRepository(private val gokulaDao: GokulaDao) {
    suspend fun insertCattle(cattle: Cattle) {
        gokulaDao.insertCattle(cattle)
    }

    suspend fun insertMilkRecord(record: MilkRecord) {
        gokulaDao.insertMilkRecord(record)
    }

    fun getMilkRecords(cattleId: Int): Flow<List<MilkRecord>> {
        return gokulaDao.getMilkRecordsForCattle(cattleId)
    }
    fun getAllCattle(): Flow<List<Cattle>> {
        return gokulaDao.getAllCattle()
    }

    suspend fun deleteCattle(cattle: Cattle) {
        gokulaDao.deleteCattle(cattle)
    }

    // Add these inside the GokulaRepository class
    suspend fun insertVaccination(vaccination: Vaccination) {
        gokulaDao.insertVaccination(vaccination)
    }

    fun getVaccinationsForCattle(cattleId: Int): Flow<List<Vaccination>> {
        return gokulaDao.getVaccinationsForCattle(cattleId)
    }

    suspend fun deleteVaccination(vaccination: Vaccination) {
        gokulaDao.deleteVaccination(vaccination)
    }
}