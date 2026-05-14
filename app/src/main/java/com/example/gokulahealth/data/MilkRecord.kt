package com.example.gokulahealth.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "milk_diary_table")
data class MilkRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cattleId: Int,
    val date: String,
    val morningYield: Double,
    val eveningYield: Double,

    // Financial tracking columns
    val grossProfit: Double,
    val expenses: Double,
    val netProfit: Double
)