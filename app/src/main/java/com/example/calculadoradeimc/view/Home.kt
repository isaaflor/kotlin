package com.example.calculadoradeimc.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.calculadoradeimc.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    mainViewModel: MainViewModel,
    onHistoryClick: () -> Unit
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    // --- PERMISSION LAUNCHER (Android 13+) ---
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            mainViewModel.scheduleWeeklyReminder(context)
            Toast.makeText(context, "Lembrete semanal ativado!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permissão necessária para lembretes.", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to handle the Click logic
    fun onEnableReminderClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if already granted
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                mainViewModel.scheduleWeeklyReminder(context)
                Toast.makeText(context, "Lembrete agendado!", Toast.LENGTH_SHORT).show()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Android 12 or below (No permission needed)
            mainViewModel.scheduleWeeklyReminder(context)
            Toast.makeText(context, "Lembrete semanal ativado!", Toast.LENGTH_SHORT).show()
        }
    }

    val activityOptions = listOf(
        "Sedentário" to 1.2,
        "Leve (1-3 dias)" to 1.375,
        "Moderado (3-5 dias)" to 1.55,
        "Ativo (6-7 dias)" to 1.725
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Calculadora de Saúde") },
                actions = {
                    // NEW: Notification Button
                    IconButton(onClick = { onEnableReminderClick() }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Ativar Lembrete", tint = Color.White)
                    }
                    // History Button
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Histórico", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Blue, titleContentColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Row 1: Height and Weight with Validation ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = uiState.height,
                    onValueChange = { mainViewModel.onHeightChange(it) },
                    label = { Text("Altura (cm)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.heightError != null,
                    supportingText = { uiState.heightError?.let { Text(it) } }
                )
                OutlinedTextField(
                    value = uiState.weight,
                    onValueChange = { mainViewModel.onWeightChange(it) },
                    label = { Text("Peso (kg)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = uiState.weightError != null,
                    supportingText = { uiState.weightError?.let { Text(it) } }
                )
            }

            // --- Row 2: Age and Gender ---
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                OutlinedTextField(
                    value = uiState.age,
                    onValueChange = { mainViewModel.onAgeChange(it) },
                    label = { Text("Idade") },
                    modifier = Modifier.weight(0.4f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.ageError != null,
                    supportingText = { uiState.ageError?.let { Text(it) } }
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Gender Buttons
                Row(
                    modifier = Modifier.weight(0.6f).padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(selected = uiState.isMale, onClick = { mainViewModel.onGenderChange(true) }, label = { Text("Homem") })
                    FilterChip(selected = !uiState.isMale, onClick = { mainViewModel.onGenderChange(false) }, label = { Text("Mulher") })
                }
            }

            // --- Row 3: Activity Dropdown ---
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = activityOptions.find { it.second == uiState.activityFactor }?.first ?: "Selecione",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Nível de Atividade") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    activityOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.first) },
                            onClick = {
                                mainViewModel.onActivityChange(option.second)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // --- Calculate Button ---
            Button(
                onClick = { mainViewModel.onCalculateClick() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(text = "CALCULAR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            // --- Global Error (if any) ---
            if (uiState.globalError != null) {
                Text(
                    text = uiState.globalError!!,
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // --- Results ---
            if (uiState.result != null) {
                ResultCard(uiState.result!!)
                BmiReferenceTable()
            }
        }
    }
}

// --- Helper Composables ---

@Composable
fun ResultCard(result: com.example.calculadoradeimc.domain.HealthMetricsResult) {
    val bmiColor = getBmiColor(result.bmi)

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text(text = "SEUS RESULTADOS", fontWeight = FontWeight.Bold, color = Color.Blue)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("IMC:", fontWeight = FontWeight.SemiBold)
                Text("%.2f".format(result.bmi), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = bmiColor)
            }
            Text(result.classification, fontSize = 14.sp, color = bmiColor, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))

            Spacer(modifier = Modifier.height(8.dp))
            ResultRow("Peso Ideal:", "%.1f kg".format(result.idealWeight))
            ResultRow("Metabolismo Basal:", "%.0f kcal".format(result.bmr))
            ResultRow("Calorias Diárias:", "%.0f kcal".format(result.dailyCalories))
        }
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = Color.Gray)
        Text(text = value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BmiReferenceTable() {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Referência IMC (WHO)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("• < 18.5: Abaixo do peso (Azul)", fontSize = 12.sp, color = Color.Gray)
            Text("• 18.5 - 24.9: Normal (Verde)", fontSize = 12.sp, color = Color.Gray)
            Text("• 25.0 - 29.9: Sobrepeso (Laranja)", fontSize = 12.sp, color = Color.Gray)
            Text("• > 30.0: Obesidade (Vermelho)", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

// Logic shared for Colors
fun getBmiColor(bmi: Double): Color {
    return when {
        bmi < 18.5 -> Color(0xFF2196F3) // Blue
        bmi < 25.0 -> Color(0xFF4CAF50) // Green
        bmi < 30.0 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336)       // Red
    }
}