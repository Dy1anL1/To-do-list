package com.example.fit5046assignment.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fit5046assignment.viewmodel.ReportViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fit5046assignment.viewmodel.DateFilter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import java.util.Locale
import kotlin.math.min
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

// Data class for Task Status Summary
data class TaskStatusSummaryEntry(
    val statusName: String,        // e.g., "Upcoming Tasks", "Overdue Tasks"
    val totalTasks: Int,           // Total number of tasks for this status
    val importantTasks: Int,       // Number of important tasks included in totalTasks
    // val color: Color            // Optional: if you want to specify colours per category
)

//Report page
@OptIn(ExperimentalMaterial3Api::class)
// @Preview(showBackground = true) // Preview might need adjustments if ViewModel is complex
@Composable
fun Report(
    onNavigateHome: () -> Unit = {},
    viewModel: ReportViewModel = viewModel()
) {
    val taskSummaryData by viewModel.taskStatusSummary.collectAsState()
    val completionPercentage by viewModel.completionPercentage.collectAsState()
    val selectedFilter by viewModel.selectedDateFilter.collectAsState()
    var showAdviceDialog by remember { mutableStateOf(false) }

    // Call AdviceDialog here
    AdviceDialog(showDialog = showAdviceDialog, completionPercentage = completionPercentage, onDismiss = { showAdviceDialog = false })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { onNavigateHome() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Home")
                    }
                },
                actions = {
                    // Add the advice button as an IconButton in the actions area
                    IconButton(
                        onClick = { showAdviceDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Advice",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "Report",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Task Status Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            // Filter Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.setDateFilter(DateFilter.ALL) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedFilter == DateFilter.ALL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("All")
                }
                Button(
                    onClick = { viewModel.setDateFilter(DateFilter.LAST_1_WEEK) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedFilter == DateFilter.LAST_1_WEEK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("Last 1 Week")
                }
                Button(
                    onClick = { viewModel.setDateFilter(DateFilter.LAST_2_WEEKS) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedFilter == DateFilter.LAST_2_WEEKS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("Last 2 Weeks")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Display Completion Percentage
            Text(
                text = "Overall Completion: ${String.format(Locale.getDefault(), "%.1f", completionPercentage)}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (taskSummaryData.isEmpty()) {
                Text("No task data available for the selected period.")
            } else {
                // Pie Chart
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Task Distribution",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val totalTasks = taskSummaryData.sumOf { it.totalTasks }
                        
                        if (totalTasks > 0) {
                            // Pie Chart visualization
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                PieChart(
                                    data = taskSummaryData.map { it.totalTasks.toFloat() },
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.error,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                                
                                // Center text showing total tasks
                                Text(
                                    text = "Total\n$totalTasks",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium,
                                    lineHeight = 20.sp
                                )
                            }
                            
                            // Legend
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            taskSummaryData.forEachIndexed { index, entry ->
                                val color = when (index) {
                                    0 -> MaterialTheme.colorScheme.primary
                                    1 -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.tertiary
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(color, RoundedCornerShape(4.dp))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${entry.statusName}: ${entry.totalTasks} (${
                                            String.format(
                                                "%.1f",
                                                if (totalTasks > 0) entry.totalTasks.toFloat() / totalTasks * 100 else 0f
                                            )
                                        }%)",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "No tasks available for visualization",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Bar Chart 
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Important vs Regular Tasks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val hasData = taskSummaryData.any { it.totalTasks > 0 }
                        
                        if (hasData) {
                            // For each category (Upcoming, Overdue, Completed)
                            taskSummaryData.forEach { entry ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = entry.statusName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    if (entry.totalTasks > 0) {
                                        // Create a stacked bar
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(30.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                        ) {
                                            // Important Tasks
                                            Box(
                                                modifier = Modifier
                                                    .weight(entry.importantTasks.toFloat().coerceAtLeast(0.01f))
                                                    .fillMaxHeight()
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                            
                                            // Regular Tasks
                                            val regularTasks = (entry.totalTasks - entry.importantTasks).coerceAtLeast(0)
                                            if (regularTasks > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .weight(regularTasks.toFloat())
                                                        .fillMaxHeight()
                                                        .background(MaterialTheme.colorScheme.tertiary)
                                                )
                                            }
                                        }
                                        
                                        // Labels below the bar
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp)
                                        ) {
                                            Text(
                                                text = "Important: ${entry.importantTasks}",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.weight(1f)
                                            )
                                            
                                            Text(
                                                text = "Regular: ${entry.totalTasks - entry.importantTasks}",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.weight(1f),
                                                textAlign = TextAlign.End
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "No tasks in this category",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "No tasks available for visualization",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(
    data: List<Float>,
    colors: List<Color>
) {
    val total = data.sum()
    
    Canvas(
        modifier = Modifier
            .size(180.dp)
            .aspectRatio(1f)
    ) {
        if (total <= 0f) return@Canvas
        
        val width = size.width
        val height = size.height
        val radius = min(width, height) / 2f
        val center = Offset(width / 2f, height / 2f)
        
        // Draw circle outline
        drawCircle(
            color = Color.LightGray.copy(alpha = 0.2f),
            radius = radius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
        
        var startAngle = -90f // Start from top (12 o'clock position)
        data.forEachIndexed { index, value ->
            if (value <= 0f) return@forEachIndexed
            
            val sweepAngle = 360f * (value / total)
            val color = colors[index % colors.size]
            
            // Draw the arc segment
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            
            // Move to the next start angle
            startAngle += sweepAngle
        }
        
        // Draw white center circle to create a donut chart effect
        drawCircle(
            color = Color.White,
            radius = radius * 0.6f,
            center = center
        )
    }
}

@Composable
fun AdviceDialog(showDialog: Boolean, completionPercentage: Float, onDismiss: () -> Unit) {
    if (showDialog) {
        val adviceText = when {
            completionPercentage < 50 -> "Keep pushing! Every task completed is a step forward. Try breaking down larger tasks into smaller, more manageable ones."
            completionPercentage < 76 -> "Good progress! You're getting things done. Maintain your momentum and focus on your priorities to reach your goals."
            else -> "Excellent work! You're on top of your tasks. Keep up the great habits and enjoy your accomplishments!"
        }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Friendly Advice") },
            text = { Text(adviceText) },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Got it!")
                }
            }
        )
    }
}

