package com.example.fit5046assignment.ui.screen

import android.app.Application
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fit5046assignment.R
import com.example.fit5046assignment.roomDb.TaskEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Reusing the DeletedTaskInfo data class from MyDayScreen
// If it's not accessible from here, you need to define it again
data class DeletedTaskInfo2(
    val task: TaskEntity,
    val position: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportantScreen(navController: NavController) {
    // Setup context for ViewModel
    val context = LocalContext.current
    val viewModel: TaskViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TaskViewModel(context.applicationContext as Application) as T
            }
        }
    )

    // Collect tasks from the ViewModel
    val allTasks by viewModel.allTasks.collectAsState(initial = emptyList())

    // Filter important tasks
    val importantTasks = remember(allTasks) {
        allTasks.filter { it.isImportant }
    }

    var expanded by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    // States for success messages and animations
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var activeMessageJob by remember { mutableStateOf<Job?>(null) }

    // Create a SnackbarHostState for showing undo options
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Save recently deleted task information for undo
    var lastDeletedTaskInfo by remember { mutableStateOf<DeletedTaskInfo?>(null) }

    // Check for overdue tasks when entering screen
    LaunchedEffect(Unit) {
        viewModel.updateOverdueStatus()
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AddTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onConfirm = { taskName, dueDate ->
                    // Create a new task with important flag set to true
                    val task = TaskEntity(
                        name = taskName,
                        dueDate = dueDate,
                        isImportant = true, // Always mark as important since this is the Important screen
                        creationDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(java.util.Date())
                    )
                    viewModel.insertTask(task)

                    // Cancel any active message job
                    activeMessageJob?.cancel()

                    // Show success message
                    successMessage = "Task \"${task.name}\" added successfully!"
                    showSuccessMessage = true
                    activeMessageJob = coroutineScope.launch {
                        delay(2000) // Show for 2 seconds
                        showSuccessMessage = false
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("home") {
                            popUpTo("important") { inclusive = true }
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = Color(0xFFBBDEFB)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // 主要内容区域
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize(),
            ) {
                Text(
                    text = "Important",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (importantTasks.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.importantlist),
                            contentDescription = "Important Tasks Image",
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Try adding a star for the tasks so you can see them here",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        itemsIndexed(importantTasks) { index, task ->
                            ImportantTaskItem(
                                task = task,
                                onTaskUpdated = { updatedTask ->
                                    // 任务更新逻辑保持不变
                                    val originalTask = task
                                    viewModel.updateTask(updatedTask)

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
                                        successMessage = "Task unmarked as important"
                                        showSuccessMessage = true
                                        activeMessageJob = coroutineScope.launch {
                                            delay(2000)
                                            showSuccessMessage = false
                                        }
                                    }
                                },
                                onTaskDeleted = {
                                    // 任务删除逻辑保持不变
                                    activeMessageJob?.cancel()

                                    lastDeletedTaskInfo = DeletedTaskInfo(
                                        task = task,
                                        position = index
                                    )

                                    viewModel.deleteTask(task)

                                    successMessage = "Task \"${task.name}\" deleted"
                                    showSuccessMessage = true

                                    activeMessageJob = coroutineScope.launch {
                                        delay(300)

                                        val result = snackbarHostState.showSnackbar(
                                            message = "Task \"${task.name}\" deleted",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Long
                                        )

                                        if (result == SnackbarResult.ActionPerformed) {
                                            lastDeletedTaskInfo?.let {
                                                viewModel.insertTask(it.task)

                                                activeMessageJob?.cancel()
                                                successMessage = "Task \"${it.task.name}\" restored"
                                                showSuccessMessage = true
                                                activeMessageJob = coroutineScope.launch {
                                                    delay(2000)
                                                    showSuccessMessage = false
                                                }
                                            }
                                        }

                                        if (showSuccessMessage && successMessage == "Task \"${task.name}\" deleted") {
                                            showSuccessMessage = false
                                        }
                                    }
                                }
                            )
                            Divider()
                        }
                    }
                }
            }

            // Add the animated success message card
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
                    contentAlignment = Alignment.Center // 使卡片居中
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
fun ImportantTaskItem(
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
        // Task completion status checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable {
                    onTaskUpdated(task.copy(isCompleted = !task.isCompleted))
                }
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
            if (task.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            } else if (task.isOverdue) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Overdue",
                    tint = Color.Red,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

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
                    color = when {
                        task.isOverdue -> Color.Red
                        task.isCompleted -> Color.Gray
                        else -> Color.Gray
                    },
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            )
        }

        IconButton(onClick = {
            // cancel star label
            onTaskUpdated(task.copy(isImportant = false))
        }) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Important",
                tint = Color.Blue
            )
        }

        IconButton(onClick = onTaskDeleted) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.Gray
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var taskName by remember { mutableStateOf("") }

    // The default date is the current date.
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // Control whether to display the date picker dialog
    var showDatePicker by remember { mutableStateOf(false) }

    // Add an error status
    var showDateError by remember { mutableStateOf(false) }

    // Get today's date timestamp
    val today = LocalDate.now()
    val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    // Create date picker status
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Verify if the selected date is today or in the future.
                        if (millis >= todayMillis) {
                            selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            showDateError = false
                            showDatePicker = false
                        } else {
                            // if pick a past date, error message shows up
                            showDateError = true
                        }
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    showDateError = false
                }) {
                    Text("Cancel")
                }
            }
        ) {
            Column {
                DatePicker(state = datePickerState)

                // if pick a wrong date, error message occur
                if (showDateError) {
                    Text(
                        text = "Please select today or a future date",
                        color = Color.Red,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add important task") },
        text = {
            Column {
                TextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("Task name") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Due date: ${selectedDate.format(dateFormatter)}")
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date"
                        )
                    }
                }
                // Add notification
                Text(
                    "Only today and future dates can be selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (taskName.isNotBlank()) {
                        onConfirm(taskName, selectedDate.format(dateFormatter))
                        onDismiss()
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}