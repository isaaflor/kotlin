package com.example.calculadoradeimc.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bmi_records")
data class BMIRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long,

    // --- Inputs Used ---
    val weight: Double,
    val height: Double,
    val age: Int,
    val gender: String,        // Stored as "Masculino" or "Feminino"
    val activityLevel: String, // Stored as "Sedentário", "Leve", etc.

    // --- Calculated Results ---
    val bmi: Double,
    val classification: String,
    val bmr: Double,           // Basal Metabolic Rate
    val idealWeight: Double,   // Devine Formula
    val dailyCalories: Double  // TDEE
)