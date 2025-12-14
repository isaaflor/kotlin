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
    // Collect the list of records from the ViewModel
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
                // Empty State
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum registro encontrado.", color = Color.Gray)
                }
            } else {
                // We use LazyColumn to make the whole screen scrollable
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // --- PART 1: THE CHART (Top of the list) ---
                    item {
                        BmiChart(records = historyList)
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // --- PART 2: THE LIST HEADER ---
                    item {
                        Text(
                            text = "Registros Recentes",
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // --- PART 3: THE LIST ITEMS ---
                    items(historyList) { record ->
                        // We wrap the item in a Box to add horizontal padding matching the rest of the screen
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            HistoryItem(record, onClick = { onRecordClick(record.id) })
                        }
                    }
                }
            }
        }
    }
}

// --- NEW COMPOSABLE: The Logic for the Line Chart ---
@Composable
fun BmiChart(records: List<BMIRecord>) {
    // 1. Sort Data: We need Oldest -> Newest for a time chart
    // 'remember' prevents re-sorting every time the screen redraws
    val sortedData = remember(records) { records.sortedBy { it.date } }

    // If we don't have enough points to make a line, show a message
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

    // 2. Find Min/Max values to calculate the scale
    val minDate = sortedData.first().date
    val maxDate = sortedData.last().date
    // Safety check: Prevent division by zero if dates are identical
    val timeSpan = (maxDate - minDate).coerceAtLeast(1)

    val minBmi = sortedData.minOf { it.bmi }
    val maxBmi = sortedData.maxOf { it.bmi }

    // Add some "padding" to the Y-axis so points aren't exactly on the edge
    // We also make sure the range covers 25.0 (the Overweight line) so we can draw it
    val yMin = minOf(minBmi, 24.0) - 1.0
    val yMax = maxOf(maxBmi, 26.0) + 1.0
    val bmiRange = yMax - yMin

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Evolução do IMC", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Blue)
        Spacer(modifier = Modifier.height(8.dp))

        // --- THE CANVAS: This is where we draw manually ---
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Fixed height for the chart
                .background(Color(0xFFF9F9F9), shape = MaterialTheme.shapes.medium)
        ) {
            // 'size.width' and 'size.height' give us the pixel dimensions of this canvas
            val width = size.width
            val height = size.height
            val pointRadius = 4.dp.toPx()

            // --- MATH HELPER FUNCTIONS ---

            /**
             * Converts a Date timestamp into an X-axis pixel coordinate.
             * Formula: (Date - StartDate) / TotalTimeDuration * CanvasWidth
             */
            fun getX(date: Long): Float {
                val fraction = (date - minDate).toFloat() / timeSpan.toFloat()
                return fraction * width
            }

            /**
             * Converts a BMI value into a Y-axis pixel coordinate.
             * IMPORTANT: In computer graphics, (0,0) is the TOP-LEFT corner.
             * So, Higher values (Top) need Lower Y-pixel coordinates.
             * Formula: CanvasHeight - ( (BMI - MinBMI) / TotalBMIRange * CanvasHeight )
             */
            fun getY(bmi: Double): Float {
                val fraction = (bmi - yMin).toFloat() / bmiRange.toFloat()
                return height - (fraction * height)
            }

            // --- DRAWING STEP 1: The Reference Line (BMI 25.0) ---
            val y25 = getY(25.0)
            // Only draw if the line falls within our current chart view
            if (y25 in 0f..height) {
                drawLine(
                    color = Color.Red.copy(alpha = 0.5f),
                    start = Offset(0f, y25),
                    end = Offset(width, y25),
                    strokeWidth = 2.dp.toPx(),
                    // This creates the "Dashed" effect
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f))
                )
            }

            // --- DRAWING STEP 2: The Lines and Points ---
            val path = Path()
            var isFirstPoint = true

            sortedData.forEach { record ->
                val x = getX(record.date)
                val y = getY(record.bmi)

                // Build the line path
                if (isFirstPoint) {
                    path.moveTo(x, y)
                    isFirstPoint = false
                } else {
                    path.lineTo(x, y)
                }

                // Draw the Dot
                // We reuse your existing getBmiColor logic
                val dotColor = if(record.bmi < 25) Color(0xFF4CAF50) else if(record.bmi < 30) Color(0xFFFF9800) else Color(0xFFF44336)

                drawCircle(
                    color = dotColor,
                    radius = pointRadius,
                    center = Offset(x, y)
                )
            }

            // Stroke the connected line
            drawPath(
                path = path,
                color = Color.Blue,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Legend
        Text(
            text = "Tracejado vermelho: Limite de Sobrepeso (25.0)",
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// --- EXISTING LIST ITEM COMPOSABLE ---
@Composable
fun HistoryItem(record: BMIRecord, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    // Logic for color (Copied from Home.kt for simplicity in this file)
    val bmiColor = if(record.bmi < 18.5) Color(0xFF2196F3)
    else if(record.bmi < 25.0) Color(0xFF4CAF50)
    else if(record.bmi < 30.0) Color(0xFFFF9800)
    else Color(0xFFF44336)

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