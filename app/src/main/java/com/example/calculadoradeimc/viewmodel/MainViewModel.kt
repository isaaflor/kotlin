package com.example.calculadoradeimc.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.calculadoradeimc.data.BMIDao
import com.example.calculadoradeimc.data.BMIRecord
import com.example.calculadoradeimc.domain.CalculateBMIUseCase
import com.example.calculadoradeimc.domain.HealthMetricsResult
import com.example.calculadoradeimc.worker.BMIReminderWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class BMIUiState(
    val height: String = "",
    val weight: String = "",
    val age: String = "",
    val isMale: Boolean = true,
    val activityFactor: Double = 1.2,
    val result: HealthMetricsResult? = null,
    val heightError: String? = null,
    val weightError: String? = null,
    val ageError: String? = null,
    val globalError: String? = null
)

class MainViewModel(private val dao: BMIDao) : ViewModel() {

    private val calculateBMIUseCase = CalculateBMIUseCase()
    private val _uiState = MutableStateFlow(BMIUiState())
    val uiState: StateFlow<BMIUiState> = _uiState.asStateFlow()

    // History Flow
    val historyList: StateFlow<List<BMIRecord>> = dao.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getRecordById(id: Int): Flow<BMIRecord> = dao.getRecordById(id)

    // --- User Actions ---
    fun onHeightChange(v: String) {
        if(v.length <= 3) _uiState.update { it.copy(height = v, heightError = null) }
    }
    fun onWeightChange(v: String) {
        if(v.length <= 7) _uiState.update { it.copy(weight = v, weightError = null) }
    }
    fun onAgeChange(v: String) {
        if(v.length <= 3) _uiState.update { it.copy(age = v, ageError = null) }
    }
    fun onGenderChange(isMale: Boolean) { _uiState.update { it.copy(isMale = isMale) } }
    fun onActivityChange(factor: Double) { _uiState.update { it.copy(activityFactor = factor) } }

    fun onCalculateClick() {
        if (validateInputs()) {
            val s = _uiState.value
            val metrics = calculateBMIUseCase(
                heightStr = s.height,
                weightStr = s.weight,
                ageStr = s.age,
                isMale = s.isMale,
                activityFactor = s.activityFactor
            )

            if (metrics != null) {
                _uiState.update { it.copy(result = metrics, globalError = null) }
                saveRecordToDb(s, metrics)
            } else {
                _uiState.update { it.copy(globalError = "Erro desconhecido no cálculo.") }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val s = _uiState.value

        val h = s.height.toIntOrNull()
        if (h == null || h !in 50..300) {
            _uiState.update { it.copy(heightError = "Entre 50 e 300 cm") }
            isValid = false
        }

        val w = s.weight.replace(",", ".").toDoubleOrNull()
        if (w == null || w !in 2.0..500.0) {
            _uiState.update { it.copy(weightError = "Entre 2 e 500 kg") }
            isValid = false
        }

        val a = s.age.toIntOrNull()
        if (a == null || a !in 1..120) {
            _uiState.update { it.copy(ageError = "Entre 1 e 120 anos") }
            isValid = false
        }

        return isValid
    }

    private fun saveRecordToDb(inputs: BMIUiState, results: HealthMetricsResult) {
        viewModelScope.launch {
            val record = BMIRecord(
                date = System.currentTimeMillis(),
                weight = inputs.weight.replace(",", ".").toDouble(),
                height = inputs.height.toDouble(),
                age = inputs.age.toInt(),
                gender = if(inputs.isMale) "Masculino" else "Feminino",
                activityLevel = getActivityLabel(inputs.activityFactor),
                bmi = results.bmi, classification = results.classification,
                bmr = results.bmr, idealWeight = results.idealWeight, dailyCalories = results.dailyCalories
            )
            dao.insert(record)
        }
    }

    private fun getActivityLabel(factor: Double): String {
        return when(factor) {
            1.2 -> "Sedentário"; 1.375 -> "Leve"; 1.55 -> "Moderado"; 1.725 -> "Ativo"; else -> "Outro"
        }
    }

    /** Gemini - início
     * Prompt: Update the MainViewModel to handle the background task scheduling.
     * Requirements:
     * 1. Create a function `scheduleWeeklyReminder` that interacts with Android's WorkManager.
     * 2. Build a `PeriodicWorkRequest` for the `BMIReminderWorker` with a 7-day interval.
     * 3. Use `ExistingPeriodicWorkPolicy.UPDATE` (or KEEP) to ensure that clicking the button multiple times does not create duplicate background tasks.
     * 4. Keep the context handling simple within the ViewModel scope for this implementation.
     */
    // --- WorkManager Scheduler ---
    fun scheduleWeeklyReminder(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<BMIReminderWorker>(7, TimeUnit.DAYS) //(15, TimeUnit.MINUTES)
            .addTag("bmi_reminder")
            .build()

        // UPDATE policy replaces the schedule if it exists (resetting the timer)
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "BMI_Weekly_Reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
    /** Gemini - final **/
    companion object {
        fun provideFactory(dao: BMIDao): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T { return MainViewModel(dao) as T }
        }
    }
}