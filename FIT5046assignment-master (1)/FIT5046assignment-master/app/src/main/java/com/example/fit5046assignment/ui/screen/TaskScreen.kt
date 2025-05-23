package com.example.fit5046assignment.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fit5046assignment.R
import com.example.fit5046assignment.roomDb.TaskEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale



//Task page with improved UX feedback
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    navController: NavController,
    tasks: List<TaskEntity>,
    onDeleteTask: (TaskEntity) -> Unit,
    onTaskUpdated: (TaskEntity) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    // States for success messages and animations
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var activeMessageJob by remember { mutableStateOf<Job?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Save recently deleted task information for undo
    var lastDeletedTaskInfo by remember { mutableStateOf<DeletedTaskInfo?>(null) }

    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)

    // Filter tasks from the last 14 days
    val validTasks = remember(tasks) {
        tasks.filter { task ->
            try {
                val taskDate = LocalDate.parse(task.dueDate, formatter)
                !taskDate.isBefore(today.minusDays(14))
            } catch (e: Exception) {
                false
            }
        }
    }

    val expiredTasks = remember(tasks) {
        tasks.filter { task ->
            try {
                val taskDate = LocalDate.parse(task.dueDate, formatter)
                taskDate.isBefore(today.minusDays(14))
            } catch (e: Exception) {
                false
            }
        }
    }

    LaunchedEffect(expiredTasks) {
        expiredTasks.forEach { onDeleteTask(it) }
    }

    val filteredTasks = validTasks.filter { task ->
        task.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("home") {
                            popUpTo("tasks") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                )
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = "All Tasks",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Search bar
                androidx.compose.material3.OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search tasks...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (filteredTasks.isEmpty()) {
                    Spacer(modifier = Modifier.height(150.dp))
                    Image(
                        painter = painterResource(id = R.drawable.tasklist),
                        contentDescription = "Empty Task Image",
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .align(Alignment.CenterHorizontally)
                            .size(200.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No tasks match your search or tasks older than 14 days will be deleted automatically.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn {
                        itemsIndexed(filteredTasks) { index, task ->
                            TaskItemDisplay(
                                task = task,
                                onTaskUpdated = { updatedTask ->
                                    // Task update logic with feedback
                                    val originalTask = task
                                    onTaskUpdated(updatedTask)

                                    activeMessageJob?.cancel()

                                    if (updatedTask.isCompleted != originalTask.isCompleted) {
                                        successMessage = if (updatedTask.isCompleted)
                                            "Task has been completed"
                                        else
                                            "Task marked as incomplete"

                                        showSuccessMessage = true
                                        activeMessageJob = coroutineScope.launch {
                                            delay(2000)
                                            showSuccessMessage = false
                                        }
                                    } else if (updatedTask.isImportant != originalTask.isImportant) {
                                        successMessage = if (updatedTask.isImportant)
                                            "Task marked as important"
                                        else
                                            "Task unmarked as important"

                                        showSuccessMessage = true
                                        activeMessageJob = coroutineScope.launch {
                                            delay(2000)
                                            showSuccessMessage = false
                                        }
                                    }
                                },
                                onTaskDeleted = {
                                    // Task deletion logic with undo option
                                    activeMessageJob?.cancel()

                                    lastDeletedTaskInfo = DeletedTaskInfo(
                                        task = task,
                                        position = index
                                    )

                                    onDeleteTask(task)

                                    successMessage = "Task \"${task.name}\" deleted"
                                    showSuccessMessage = true

                                    activeMessageJob = coroutineScope.launch {
                                        delay(1000)

                                        if (showSuccessMessage && successMessage == "Task \"${task.name}\" deleted") {
                                            showSuccessMessage = false
                                        }
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

            // Add the animated success message card (same as in ImportantScreen)
            AnimatedVisibility(
                visible = showSuccessMessage,
                enter = fadeIn(animationSpec = tween(300)) +
                        slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)) +
                        slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = successMessage,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItemDisplay(
    task: TaskEntity,
    onTaskUpdated: (TaskEntity) -> Unit,
    onTaskDeleted: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Completed / Overdue circular button
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable { onTaskUpdated(task.copy(isCompleted = !task.isCompleted)) }
                .border(
                    width = 2.dp,
                    color = when {
                        task.isOverdue -> Color.Red
                        task.isCompleted -> MaterialTheme.colorScheme.primary
                        else -> Color.Gray
                    },
                    shape = CircleShape
                )
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                task.isCompleted -> Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                task.isOverdue -> Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Overdue",
                    tint = Color.Red,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Task name & date
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                ),
                color = when {
                    task.isOverdue -> Color.Red
                    task.isCompleted -> Color.Gray
                    else -> MaterialTheme.colorScheme.onBackground
                }
            )
            Text(
                text = task.dueDate,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            )
        }

        // important logo
        IconButton(onClick = {
            onTaskUpdated(task.copy(isImportant = !task.isImportant))
        }) {
            Icon(
                imageVector = if (task.isImportant) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "Important",
                tint = if (task.isImportant) Color.Blue else Color.Gray
            )
        }

        // delete button
        IconButton(onClick = onTaskDeleted) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.Gray
            )
        }
    }
}