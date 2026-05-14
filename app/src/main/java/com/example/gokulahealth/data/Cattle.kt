package com.example.gokulahealth.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cattle_table")
data class Cattle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val earTagId: String,
    val name: String,
    val breed: String,
    val dateOfBirth: String,
    val imagePath: String? = null
)