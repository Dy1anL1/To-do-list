package com.example.fit5046assignment.ui.screen

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fit5046assignment.roomDb.TaskEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Create a data class to save recently deleted tasks for undo operations.
data class DeletedTaskInfo(
    val task: TaskEntity,
    val position: Int
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDayScreen(navController: NavController) {
    // Get the current date and time
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    val formattedDate = dateFormat.format(calendar.time)
    val dayOfWeek = dayFormat.format(calendar.time)

    val context = LocalContext.current
    val viewModel: TaskViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TaskViewModel(context.applicationContext as Application) as T
            }
        }
    )

    val allTasks by viewModel.allTasks.collectAsState(initial = emptyList())

    // Filter tasks to only show those with today's date
    val todayTasks = remember(allTasks, formattedDate) {
        allTasks.filter { it.dueDate == formattedDate }
    }

    var showDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    // Create a SnackbarHostState to display notifications.
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Save recently deleted task information
    var lastDeletedTaskInfo by remember { mutableStateOf<DeletedTaskInfo?>(null) }

    // Used to control the display status of success prompts.
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    // Control the coroutine scope that displays the prompt.
    var activeMessageJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // Check every time you enter the screen
    LaunchedEffect(Unit) {
        viewModel.updateOverdueStatus()
    }

    // Add Task Dialog
    if (showDialog) {
        AddTaskDialog(
            onDismiss = { showDialog = false },
            onTaskAdded = { task ->
                viewModel.insertTask(task)
                // 取消之前的消息显示任务（如果有）
                activeMessageJob?.cancel()

                // 显示成功添加任务的消息
                successMessage = "Task \"${task.name}\" added successfully!"
                showSuccessMessage = true
                activeMessageJob = coroutineScope.launch {
                    delay(2000) // 显示2秒
                    showSuccessMessage = false
                }
            },
            formattedDate = formattedDate
        )
    }

    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("home") {
                            popUpTo("my_day") { inclusive = true }
                        }
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
                onClick = { showDialog = true },
                containerColor = Color(0xFF90CAF9)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add task")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content area
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    // Add vertical scroll
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "My day",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$dayOfWeek, $formattedDate",
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                TaskList(
                    // Use the filtered todayTasks instead of all tasks. (Because this is daily task function)
                    tasks = todayTasks,
                    onTaskUpdated = { updatedTask, originalTask ->
                        viewModel.updateTask(updatedTask)

                        // Display the corresponding notification when the user performs an operation on the task.
                        if (updatedTask.isCompleted != originalTask.isCompleted) {
                            // Cancel the previous message display task (if any)
                            activeMessageJob?.cancel()

                            // Completion status change
                            successMessage = if (updatedTask.isCompleted)
                                "Task has been completed"
                            else
                                "Task marked as incomplete"

                            showSuccessMessage = true
                            activeMessageJob = coroutineScope.launch {
                                delay(2000) // show the message with 2 sec
                                showSuccessMessage = false
                            }
                        } else if (updatedTask.isImportant != originalTask.isImportant) {
                            // Cancel the previous message display task (if any)
                            activeMessageJob?.cancel()

                            // Change of importance marking
                            successMessage = if (updatedTask.isImportant)
                                "Task marked as important"
                            else
                                "Task unmarked as important"

                            showSuccessMessage = true
                            activeMessageJob = coroutineScope.launch {
                                delay(2000) // show the message within 2 sec
                                showSuccessMessage = false
                            }
                        }
                    },
                    onTaskDeleted = { task, position ->
                        // Cancel the previous message display task (if any)
                        activeMessageJob?.cancel()

                        // Save the deleted task information
                        lastDeletedTaskInfo = DeletedTaskInfo(task, position)

                        // Delete task
                        viewModel.deleteTask(task)

                        // Show Snackbar with undo option
                        successMessage = "Task \"${task.name}\" deleted"
                        showSuccessMessage = true

                        activeMessageJob = coroutineScope.launch {
                            // Use a custom SnackBar instead of the system SnackBar.
                            delay(300) // Give the animation some time
                            val result = snackbarHostState.showSnackbar(
                                message = "Task \"${task.name}\" deleted",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Long
                            )

                            // If the user clicks the undo button
                            if (result == SnackbarResult.ActionPerformed) {
                                lastDeletedTaskInfo?.let {
                                    viewModel.insertTask(it.task)

                                    // Display a prompt for recovery operations
                                    activeMessageJob?.cancel()
                                    successMessage = "Task \"${it.task.name}\" restored"
                                    showSuccessMessage = true
                                    activeMessageJob = launch {
                                        delay(2000)
                                        showSuccessMessage = false
                                    }
                                }
                            }

                            // Hide delete prompt
                            if (showSuccessMessage && successMessage == "Task \"${task.name}\" deleted") {
                                showSuccessMessage = false
                            }
                        }
                    }
                )
            }

            // Animated card displaying success message
            AnimatedVisibility(
                visible = showSuccessMessage,
                enter = fadeIn(animationSpec = tween(300)) +
                        slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)) +
                        slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center // Center the card
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
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onTaskAdded: (TaskEntity) -> Unit,
    formattedDate: String
) {
    var taskName by remember { mutableStateOf("") }
    var isImportant by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Task") },
        text = {
            Column {
                TextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("Task Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isImportant,
                        onCheckedChange = { isImportant = it }
                    )
                    Text("Important")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (taskName.isNotBlank()) {
                        val task = TaskEntity(
                            name = taskName,
                            dueDate = formattedDate,
                            isImportant = isImportant,
                            creationDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        )
                        onTaskAdded(task)
                        onDismiss()
                    }
                },
                enabled = taskName.isNotBlank()
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// task list functions
@Composable
fun TaskList(
    tasks: List<TaskEntity>,
    // Update to receive the original tasks and the updated tasks.
    onTaskUpdated: (TaskEntity, TaskEntity) -> Unit,
    onTaskDeleted: (TaskEntity, Int) -> Unit
) {
    Column {
        tasks.forEachIndexed { index, task ->
            TaskItem(
                task = task,
                onTaskUpdated = { updatedTask -> onTaskUpdated(updatedTask, task) },  // Pass on the original task
                onTaskDeleted = { onTaskDeleted(task, index) }
            )
        }
    }
}

// task item function
@Composable
fun TaskItem(
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

        // Task name and date
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

        // Important status star mark
        IconButton(
            onClick = {
                onTaskUpdated(task.copy(isImportant = !task.isImportant))
            }
        ) {
            Icon(
                imageVector = if (task.isImportant) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "Important",
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