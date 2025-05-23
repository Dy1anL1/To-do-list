package com.example.fit5046assignment.ui.screen

import android.os.Build
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import android.util.Log
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.time.format.DateTimeParseException
import java.util.Locale

// Filter Type Enumeration
enum class TaskFilter {
    THIS_WEEK,
    TOMORROW,
    OUT_OF_DATE
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanToDoScreen(
    navController: NavController,
    viewModel: TaskViewModel = viewModel(),
    tasks: List<TaskEntity> = emptyList(),
    onTaskCompleteChange: (TaskEntity, Boolean) -> Unit = { _, _ -> },
    onTaskDeleted: (TaskEntity) -> Unit = { viewModel.deleteTask(it) },
    onTaskAdded: (TaskEntity) -> Unit,
    onBackClick: () -> Unit = {},
    onAddTaskClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val write = results[Manifest.permission.WRITE_CALENDAR] ?: false
        val read  = results[Manifest.permission.READ_CALENDAR] ?: false
        if (!write || !read) {
            Toast.makeText(
                context,
                "Need Calendar permission to sync tasks to calendar",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    LaunchedEffect(Unit) {
        permLauncher.launch(
            arrayOf(
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.READ_CALENDAR
            )
        )
    }

    var topMenuExpanded by remember { mutableStateOf(false) }
    var filterMenuExpanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(TaskFilter.THIS_WEEK) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    // Add these states for success messages and animations
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var activeMessageJob by remember { mutableStateOf<Job?>(null) }

    // Create a SnackbarHostState for showing undo options
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Save recently deleted task information for undo
    var lastDeletedTaskInfo by remember { mutableStateOf<DeletedTaskInfo2?>(null) }

    // Get the current date for filtering
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
    val endOfWeek = today.plusDays(7 - today.dayOfWeek.value.toLong())

    // Date Formatter
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH) }

    // Function to parse date string into LocalDate object
    fun parseDate(dateString: String): LocalDate? {
        return try {
            // Try to parse using the application's format
            LocalDate.parse(dateString, dateFormatter)
        } catch (e: DateTimeParseException) {
            try {
                // Backup format in case the date format is inconsistent.
                LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE)
            } catch (e: DateTimeParseException) {
                // Returning null indicates that date parsing has failed.
                null
            }
        }
    }

    // Filter tasks based on the selected filters
    val filteredTasks = tasks.filter { task ->
        val taskDueDate = parseDate(task.dueDate)
        // All filters do not display completed tasks and tasks with invalid dates.
        !task.isCompleted && taskDueDate != null &&
                when (selectedFilter) {
                    TaskFilter.THIS_WEEK -> {
                        taskDueDate >= today && taskDueDate <= endOfWeek
                    }
                    TaskFilter.TOMORROW -> {
                        taskDueDate == tomorrow
                    }
                    TaskFilter.OUT_OF_DATE -> {
                        taskDueDate < today
                    }
                }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AddPlanTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onConfirm = { taskName, dueDate, isImportant ->
                    // Create a new task
                    val task = TaskEntity(
                        name = taskName,
                        dueDate = dueDate,
                        isImportant = isImportant,
                        creationDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(java.util.Date())
                    )
                    onTaskAdded(task)

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

    // UI section
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("home") {
                            popUpTo("plan_to_do") { inclusive = true }
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
        // Add the snackbar host
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // Title
                Text(
                    text = "Plan to do",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )

                // Filter hamburger button
                Box(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .align(Alignment.Start)
                ) {
                    OutlinedButton(
                        onClick = { filterMenuExpanded = true },
                        shape = RectangleShape
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Filter", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = when(selectedFilter) {
                            TaskFilter.THIS_WEEK -> "This Week"
                            TaskFilter.TOMORROW -> "Tomorrow"
                            TaskFilter.OUT_OF_DATE -> "Out of date"
                        })
                    }

                    DropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = { filterMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically){
                                    Icon(Icons.Default.Warning, contentDescription = "Out of date", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Out of date", color = Color(0xFF1976D2))
                                }
                            },
                            onClick = {
                                selectedFilter = TaskFilter.OUT_OF_DATE
                                filterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically){
                                    Icon(Icons.Default.Notifications, contentDescription = "Tomorrow", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tomorrow", color = Color(0xFF1976D2))
                                }
                            },
                            onClick = {
                                selectedFilter = TaskFilter.TOMORROW
                                filterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically){
                                    Icon(Icons.Default.DateRange, contentDescription = "This Week", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("This Week", color = Color(0xFF1976D2))
                                }
                            },
                            onClick = {
                                selectedFilter = TaskFilter.THIS_WEEK
                                filterMenuExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display the filtered task list
                if (filteredTasks.isEmpty()) {
                    Spacer(modifier = Modifier.height(150.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.plan),
                            contentDescription = "Empty Plan Tasks",
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when(selectedFilter) {
                                TaskFilter.THIS_WEEK -> "No tasks for this week!"
                                TaskFilter.TOMORROW -> "No tasks for tomorrow!"
                                TaskFilter.OUT_OF_DATE -> "No overdue tasks!"
                            },
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn {
                        items(filteredTasks) { task ->
                            PlanTaskItem(
                                task = task,
                                onTaskCompleteChange = { completed ->
                                    // Update task completion status
                                    val updatedTask = task.copy(isCompleted = completed)
                                    onTaskCompleteChange(task, completed)

                                    // Show completion message
                                    activeMessageJob?.cancel()
                                    successMessage = if (completed) "Task has been completed" else "Task marked as incomplete"
                                    showSuccessMessage = true
                                    activeMessageJob = coroutineScope.launch {
                                        delay(2000)
                                        showSuccessMessage = false
                                    }
                                },
                                onTaskDeleted = {
                                    // Save task for potential undo
                                    lastDeletedTaskInfo = DeletedTaskInfo2(
                                        task = task,
                                        position = filteredTasks.indexOf(task)
                                    )

                                    // Delete the task
                                    onTaskDeleted(task)

                                    // Show delete message
                                    activeMessageJob?.cancel()
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
                                                // Restore the deleted task
                                                onTaskAdded(it.task)

                                                // Show restored message
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
                                },
                                onToggleImportance = { isImportant ->
                                    // Handle importance toggle
                                    val updatedTask = task.copy(isImportant = isImportant)
                                    viewModel.updateTask(updatedTask)

                                    // Show importance message
                                    activeMessageJob?.cancel()
                                    successMessage = if (isImportant)
                                        "Task marked as important"
                                    else
                                        "Task unmarked as important"
                                    showSuccessMessage = true
                                    activeMessageJob = coroutineScope.launch {
                                        delay(2000)
                                        showSuccessMessage = false
                                    }
                                }
                            )
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlanTaskItem(
    task: TaskEntity,
    onTaskCompleteChange: (Boolean) -> Unit,
    onTaskDeleted: () -> Unit,
    onToggleImportance: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Task completion checkbox (matches MyDayScreen style)
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable { onTaskCompleteChange(!task.isCompleted) }
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

        // Task details
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

        // Important indicator with clickable functionality
        IconButton(onClick = { onToggleImportance(!task.isImportant) }) {
            Icon(
                imageVector = if (task.isImportant) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = if (task.isImportant) "Remove importance" else "Mark as important",
                tint = if (task.isImportant) Color.Blue else Color.Gray
            )
        }

        // Delete button
        IconButton(
            onClick = onTaskDeleted
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete task",
                tint = Color.Gray
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlanTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean) -> Unit
) {
    var taskName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isImportant by remember { mutableStateOf(false) }
    // Add an error status
    var showDateError by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH) }

    // Get today's date timestamp
    val today = LocalDate.now()
    val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    // Set the date picker status
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
        title = { Text("Add Plan Task") },
        text = {
            Column {
                TextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("Task Name") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Due Date: ${selectedDate.format(dateFormatter)}")
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

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isImportant,
                        onCheckedChange = { isImportant = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mark as Important")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (taskName.isNotBlank()) {
                        onConfirm(taskName, selectedDate.format(dateFormatter), isImportant)
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