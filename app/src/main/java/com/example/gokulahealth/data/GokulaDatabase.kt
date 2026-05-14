package com.example.gokulahealth.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Cattle::class, MilkRecord::class, Vaccination::class],
    version = 3, // Incremented to 3 to apply the new MilkRecord financial columns
    exportSchema = false
)
abstract class GokulaDatabase : RoomDatabase() {
    abstract fun gokulaDao(): GokulaDao

    companion object {
        @Volatile
        private var INSTANCE: GokulaDatabase? = null

        fun getDatabase(context: Context): GokulaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GokulaDatabase::class.java,
                    "gokula_database"
                )
                    /**
                     * Wipes the old database and creates a new one with the new schema.
                     * This is necessary because we added grossProfit, expenses, and netProfit
                     * to the MilkRecord table.
                     */
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}