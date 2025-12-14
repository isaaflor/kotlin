package com.example.calculadoradeimc.domain

/**
 * Data class holding all calculated health metrics.
 */
data class HealthMetricsResult(
    val bmi: Double,
    val classification: String,
    val bmr: Double,
    val idealWeight: Double,
    val dailyCalories: Double
)

/**
 * Use Case: Encapsulates the business logic for Health Calculations.
 *
 * Formulas Used:
 * 1. BMI (Body Mass Index): Weight / Height²
 * 2. BMR (Basal Metabolic Rate): Mifflin-St Jeor Equation (considered most accurate).
 * - Men: (10 × weight) + (6.25 × height) - (5 × age) + 5
 * - Women: (10 × weight) + (6.25 × height) - (5 × age) - 161
 * 3. Ideal Weight: Devine Formula (1974).
 * - Men: 50kg + 2.3kg per inch over 5 feet.
 * - Women: 45.5kg + 2.3kg per inch over 5 feet.
 */
class CalculateBMIUseCase {

    /**
     * Executes the calculation logic.
     *
     * @param heightStr Height in Centimeters (cm).
     * @param weightStr Weight in Kilograms (kg).
     * @param ageStr Age in Years.
     * @param isMale Boolean indicating gender (True = Male, False = Female).
     * @param activityFactor Multiplier for TDEE (1.2 to 1.9).
     * @return [HealthMetricsResult] if inputs are valid, or null.
     */
    operator fun invoke(
        heightStr: String,
        weightStr: String,
        ageStr: String,
        isMale: Boolean,
        activityFactor: Double
    ): HealthMetricsResult? {
        val weight = weightStr.replace(",", ".").toDoubleOrNull()
        val height = heightStr.toDoubleOrNull()
        val age = ageStr.toIntOrNull()

        if (weight != null && height != null && age != null && height > 0) {

            // --- 1. BMI Calculation ---
            val heightInMeters = height / 100
            val bmi = weight / (heightInMeters * heightInMeters)

            // Cascade logic for accurate decimal handling
            val classification = when {
                bmi < 18.5 -> "Abaixo do peso"
                bmi < 25.0 -> "Peso normal"
                bmi < 30.0 -> "Sobrepeso"
                bmi < 35.0 -> "Obesidade (Grau 1)"
                bmi < 40.0 -> "Obesidade severa (Grau 2)"
                else -> "Obesidade Mórbida (Grau 3)"
            }

            // --- 2. BMR (Basal Metabolic Rate) - Mifflin-St Jeor ---
            var bmr = (10 * weight) + (6.25 * height) - (5 * age)
            bmr += if (isMale) 5 else -161

            // --- 3. Ideal Weight - Devine Formula ---
            // 5 feet = 60 inches. 1 inch = 2.54 cm.
            val heightInInches = height / 2.54
            val inchesOver5Feet = heightInInches - 60

            var idealWeight = if (isMale) 50.0 else 45.5
            if (inchesOver5Feet > 0) {
                idealWeight += (2.3 * inchesOver5Feet)
            }

            // --- 4. Daily Calorie Needs (TDEE) ---
            val dailyCalories = bmr * activityFactor

            return HealthMetricsResult(
                bmi = bmi,
                classification = classification,
                bmr = bmr,
                idealWeight = idealWeight,
                dailyCalories = dailyCalories
            )
        }
        return null
    }
}