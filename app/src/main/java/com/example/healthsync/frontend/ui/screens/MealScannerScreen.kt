package com.example.healthsync.frontend.ui.screens;

import android.graphics.BitmapFactory
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthsync.frontend.data.local.MealEntity
import com.example.healthsync.frontend.data.repository.MealRepository
import com.example.healthsync.frontend.ui.viewmodels.MealScannerUiState
import com.example.healthsync.frontend.ui.viewmodels.MealScannerViewModel
import androidx.compose.ui.graphics.asImageBitmap
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealScannerScreen(
    viewModel: MealScannerViewModel,
    imagePath: String,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val bitmap = remember(imagePath) { BitmapFactory.decodeFile(imagePath) }
    var scanMode by remember { mutableStateOf("FOOD") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HealthSync Scanner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Selector
            if (uiState is MealScannerUiState.Idle) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    ModeButton("Food Item", scanMode == "FOOD", Modifier.weight(1f)) { scanMode = "FOOD" }
                    ModeButton("Nutrition Label", scanMode == "LABEL", Modifier.weight(1f)) { scanMode = "LABEL" }
                }
            }

            // Image Preview
            Card(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (uiState is MealScannerUiState.Loading) {
                        Box(Modifier.fillMaxSize().background(Color.Black.copy(0.5f)), Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF00BCD4))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (val state = uiState) {
                is MealScannerUiState.Idle -> {
                    Button(
                        onClick = { 
                            bitmap?.let { 
                                if (scanMode == "FOOD") viewModel.analyzeFood(it)
                                else viewModel.analyzeLabel(it, imagePath)
                            } 
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Search, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (scanMode == "FOOD") "Identify Food" else "Scan Label", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                is MealScannerUiState.Loading -> {
                    Text(if (scanMode == "FOOD") "Identifying specific food..." else "Extracting nutrition data...", color = Color.Gray)
                }
                is MealScannerUiState.Success -> {
                    SuccessView(state.result, imagePath, viewModel, onSaved)
                }
                is MealScannerUiState.Error -> {
                    ErrorView(state.message) { viewModel.reset() }
                }
            }
        }
    }
}

@Composable
fun SuccessView(result: MealRepository.AnalysisResult, imagePath: String, viewModel: MealScannerViewModel, onSaved: () -> Unit) {
    val meal = result.meal
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(meal.mealName.uppercase(), color = Color(0xFF00BCD4), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                    Badge(containerColor = Color(0xFF00BCD4).copy(0.2f)) {
                        Text(result.source, color = Color(0xFF00BCD4), modifier = Modifier.padding(4.dp))
                    }
                }
                
                if (result.confidence > 0) {
                    Text("Confidence: ${(result.confidence * 100).toInt()}%", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(Modifier.height(16.dp))
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    NutrientMiniItem("Calories", "${meal.calories.toInt()} kcal", Icons.Default.Info)
                    NutrientMiniItem("Protein", "${meal.protein.toInt()}g", Icons.Default.Star)
                    NutrientMiniItem("Carbs", "${meal.carbs.toInt()}g", Icons.Default.Menu)
                }

                if (result.rawOcrText != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFF333333))
                    Text("Scanned Text", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(result.rawOcrText, color = Color.Gray, fontSize = 11.sp, maxLines = 3, modifier = Modifier.padding(top = 4.dp))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFF333333))

                Text("Detailed Breakdown", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                
                NutrientRow("Fat", "${meal.fat}g")
                NutrientRow("Fiber", "${meal.fiber}g")
                NutrientRow("Sugar", "${meal.sugar}g")
                NutrientRow("Sodium", "${meal.sodium}mg")
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = { viewModel.reset() },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Text("Retry", color = Color.White)
            }
            Button(
                onClick = { 
                    viewModel.saveMeal(meal.copy(imagePath = imagePath))
                    onSaved()
                },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Meal", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ModeButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF00BCD4) else Color.Transparent,
            contentColor = if (isSelected) Color.Black else Color.Gray
        ),
        shape = RoundedCornerShape(10.dp),
        elevation = null
    ) {
        Text(text, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun NutrientMiniItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Color(0xFF00BCD4), modifier = Modifier.size(20.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun NutrientRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), Arrangement.SpaceBetween) {
        Text(label, color = Color.LightGray)
        Text(value, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text(message, color = Color.White, textAlign = TextAlign.Center, fontSize = 16.sp)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry, shape = RoundedCornerShape(12.dp)) {
            Text("Try Another Image")
        }
    }
}
