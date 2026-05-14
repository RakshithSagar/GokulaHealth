package com.example.gokulahealth.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vaccinations")
data class Vaccination(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cattleId: Int,
    val vaccineName: String, // e.g., "FMD", "Brucellosis" [cite: 26]
    val dueDate: Long, // Timestamp for the alarm
    val isCompleted: Boolean = false
)