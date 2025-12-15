package com.example.calculadoradeimc.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculadoradeimc.data.BMIRecord
import com.example.calculadoradeimc.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onRecordClick: (Int) -> Unit
) {
    val historyList by viewModel.historyList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Blue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            if (historyList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum registro encontrado.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // --- CHART SECTION ---
                    item {
                        BmiChart(records = historyList)
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // --- HEADER SECTION ---
                    item {
                        Text(
                            text = "Registros Recentes",
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // --- LIST ITEMS ---
                    items(historyList) { record ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            HistoryItem(record, onClick = { onRecordClick(record.id) })
                        }
                    }
                }
            }
        }
    }
}

// --- UPDATED CHART LOGIC ---
/** Gemini - início
 * Prompt: Implement a Visual Analytics feature using Native Jetpack Compose Canvas (no external libraries like MPAndroidChart).
 * Requirements:
 * 1. Draw a Line Chart showing BMI evolution over time (X=Date, Y=BMI).
 * 2. Use visual data points (circles) connected by lines.
 * 3. Apply dynamic colors to points based on BMI category (Blue < 18.5, Green < 25, Orange < 30, Red >= 30).
 * 4. Draw horizontal threshold lines at Y=18.5 and Y=30.0 for visual reference.
 * 5. Keep the implementation simple, performant, and contained within a reusable Composable.
 */
@Composable
fun BmiChart(records: List<BMIRecord>) {
    // 1. Sort Data: Oldest -> Newest
    val sortedData = remember(records) { records.sortedBy { it.date } }

    if (sortedData.size < 2) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
                .background(Color(0xFFF0F0F0), shape = MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text("Insira ao menos 2 registros para ver o gráfico.", color = Color.Gray, fontSize = 12.sp)
        }
        return
    }

    // 2. Calculate Scales (Min/Max)
    val minDate = sortedData.first().date
    val maxDate = sortedData.last().date
    val timeSpan = (maxDate - minDate).coerceAtLeast(1)

    val minDataBmi = sortedData.minOf { it.bmi }
    val maxDataBmi = sortedData.maxOf { it.bmi }

    // FORCE SCALE: Ensure the chart always covers the range [18.0 to 31.0]
    // regardless of the user's data. This guarantees the reference lines
    // (18.5 and 30.0) are always visible inside the canvas.
    val yMin = minOf(minDataBmi, 18.0) - 1.0
    val yMax = maxOf(maxDataBmi, 31.0) + 1.0
    val bmiRange = yMax - yMin

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Evolução do IMC", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Blue)
        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp) // Slightly taller to accommodate wider range
                .background(Color(0xFFFAFAFA), shape = MaterialTheme.shapes.medium)
        ) {
            val width = size.width
            val height = size.height
            val pointRadius = 4.dp.toPx()

            // --- MATH HELPER FUNCTIONS ---
            fun getX(date: Long): Float {
                val fraction = (date - minDate).toFloat() / timeSpan.toFloat()
                return fraction * width
            }

            fun getY(bmi: Double): Float {
                // Invert Y axis (0 is top)
                val fraction = (bmi - yMin).toFloat() / bmiRange.toFloat()
                return height - (fraction * height)
            }

            // --- DRAWING: REFERENCE LINES ---
            // Helper to draw dashed lines
            fun drawThreshold(value: Double, color: Color) {
                val yPos = getY(value)
                if (yPos in 0f..height) {
                    drawLine(
                        color = color.copy(alpha = 0.6f),
                        start = Offset(0f, yPos),
                        end = Offset(width, yPos),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f))
                    )
                }
            }

            drawThreshold(18.5, Color(0xFF2196F3)) // Blue: Underweight boundary
            drawThreshold(25.0, Color(0xFFFF9800)) // Orange: Overweight boundary
            drawThreshold(30.0, Color(0xFFF44336)) // Red: Obesity boundary

            // --- DRAWING: DATA PATH ---
            val path = Path()
            var isFirstPoint = true

            sortedData.forEach { record ->
                val x = getX(record.date)
                val y = getY(record.bmi)

                if (isFirstPoint) {
                    path.moveTo(x, y)
                    isFirstPoint = false
                } else {
                    path.lineTo(x, y)
                }
            }

            // Draw Blue Line connecting dots
            drawPath(
                path = path,
                color = Color.Blue,
                style = Stroke(width = 2.dp.toPx())
            )

            // --- DRAWING: DATA POINTS (With Color Logic) ---
            sortedData.forEach { record ->
                val x = getX(record.date)
                val y = getY(record.bmi)

                // STRICT COLOR LOGIC
                val dotColor = when {
                    record.bmi < 18.5 -> Color(0xFF2196F3) // Blue
                    record.bmi < 25.0 -> Color(0xFF4CAF50) // Green
                    record.bmi < 30.0 -> Color(0xFFFF9800) // Orange
                    else -> Color(0xFFF44336)              // Red
                }

                drawCircle(
                    color = dotColor,
                    radius = pointRadius,
                    center = Offset(x, y)
                )
            }
        }

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendItem(color = Color(0xFF2196F3), text = "18.5")
            LegendItem(color = Color(0xFF4CAF50), text = "Normal")
            LegendItem(color = Color(0xFFFF9800), text = "25.0")
            LegendItem(color = Color(0xFFF44336), text = "30.0")
        }
    }
}
/** Gemini - final */
@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, shape = androidx.compose.foundation.shape.CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 10.sp, color = Color.Gray)
    }
}

// --- UPDATED LIST ITEM ---
@Composable
fun HistoryItem(record: BMIRecord, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    // Standard Color Logic
    val bmiColor = when {
        record.bmi < 18.5 -> Color(0xFF2196F3) // Blue
        record.bmi < 25.0 -> Color(0xFF4CAF50) // Green
        record.bmi < 30.0 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336)              // Red
    }

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(dateFormat.format(Date(record.date)), fontSize = 12.sp, color = Color.Gray)
                Text(record.classification, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Text(
                text = "IMC: %.2f".format(record.bmi),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = bmiColor
            )
        }
    }
}