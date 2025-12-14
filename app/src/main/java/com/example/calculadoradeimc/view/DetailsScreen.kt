package com.example.calculadoradeimc.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculadoradeimc.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    viewModel: MainViewModel,
    recordId: Int,
    onBack: () -> Unit
) {
    // Collect the specific record from DB
    val record by viewModel.getRecordById(recordId).collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Registro") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
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
                .padding(16.dp)
        ) {
            if (record == null) {
                Text("Carregando...", modifier = Modifier.padding(16.dp))
            } else {
                val r = record!! // Safe because we checked null
                val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())

                // USES THE FUNCTION FROM Home.kt (Shared in package)
                val bmiColor = getBmiColor(r.bmi)

                // Header Date
                Text(
                    text = dateFormat.format(Date(r.date)),
                    fontSize = 14.sp, color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Main Result
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text("RESULTADO FINAL", fontWeight = FontWeight.Bold, color = Color.Blue)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("IMC", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("%.2f".format(r.bmi), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = bmiColor)
                        }
                        Text(r.classification, fontWeight = FontWeight.SemiBold, color = bmiColor)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Inputs Section
                SectionTitle("Dados de Entrada")
                InfoRow("Idade / Gênero", "${r.age} anos | ${r.gender}")
                InfoRow("Peso / Altura", "${r.weight} kg | ${r.height} cm")
                InfoRow("Atividade", r.activityLevel)

                Spacer(modifier = Modifier.height(16.dp))

                // Advanced Results
                SectionTitle("Métricas Avançadas")
                InfoRow("Peso Ideal (Devine)", "%.1f kg".format(r.idealWeight))
                InfoRow("Metabolismo Basal", "%.0f kcal".format(r.bmr))
                InfoRow("Calorias Diárias", "%.0f kcal".format(r.dailyCalories))
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
    }
}