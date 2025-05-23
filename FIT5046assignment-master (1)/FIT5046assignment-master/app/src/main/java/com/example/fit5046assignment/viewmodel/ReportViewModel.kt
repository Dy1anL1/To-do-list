package com.example.fit5046assignment.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fit5046assignment.roomDb.TaskEntity
import com.example.fit5046assignment.roomDb.TaskRepository
import com.example.fit5046assignment.ui.screen.TaskStatusSummaryEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Define an enum for the filter options
enum class DateFilter {
    ALL, LAST_1_WEEK, LAST_2_WEEKS
}

/**
 * ViewModel for the Report Screen.
 * This ViewModel is responsible for fetching task data from the [TaskRepository],
 * processing it into a summary format suitable for the report UI, and exposing it as a [StateFlow].
 * It extends [AndroidViewModel] to have access to the application context, which is needed for the repository.
 *
 * @param application The application instance, used to initialize the [TaskRepository].
 */
class ReportViewModel(application: Application) : AndroidViewModel(application) {

    // Instance of the TaskRepository to interact with the data layer.
    private val repository: TaskRepository = TaskRepository(application)

    // Date formatter for parsing task due dates. Use the same format as in TaskViewModel.
    private val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

    private val _selectedDateFilter = MutableStateFlow(DateFilter.ALL)
    val selectedDateFilter: StateFlow<DateFilter> = _selectedDateFilter

    fun setDateFilter(filter: DateFilter) {
        _selectedDateFilter.value = filter
    }

    /**
     * A [StateFlow] that emits a list of [TaskStatusSummaryEntry] objects.
     * This flow represents the summarized task data to be displayed on the Report Screen.
     * It is derived from the `allTasks` flow from the repository and transformed
     * based on the selected date filter.
     */
    val taskStatusSummary: StateFlow<List<TaskStatusSummaryEntry>> = repository.allTasks
        .combine(_selectedDateFilter) { tasks, filter ->
            val currentDate = Calendar.getInstance().time
            val filteredTasks = tasks.filter { task ->
                if (filter == DateFilter.ALL) {
                    true
                } else {
                    try {
                        val dueDate = dateFormat.parse(task.dueDate)
                        if (dueDate != null) {
                            val calendar = Calendar.getInstance()
                            calendar.time = currentDate
                            
                            val startDate = when (filter) {
                                DateFilter.LAST_1_WEEK -> {
                                    calendar.add(Calendar.WEEK_OF_YEAR, -1)
                                    calendar.time
                                }
                                DateFilter.LAST_2_WEEKS -> {
                                    calendar.add(Calendar.WEEK_OF_YEAR, -2)
                                    calendar.time
                                }
                                else -> Date(0) // Earliest possible date
                            }
                            
                            // Task due date is within the window [startDate, currentDate]
                            !dueDate.before(startDate) && !dueDate.after(currentDate)
                        } else {
                            false
                        }
                    } catch (e: Exception) {
                        println("Error parsing date for task during filter: ${task.name}, dueDate: ${task.dueDate}, Error: ${e.message}")
                        false // Exclude tasks with unparseable dates from filtered results
                    }
                }
            }

            val upcomingTasks = mutableListOf<TaskEntity>()
            val overdueTasks = mutableListOf<TaskEntity>()
            val completedTasks = mutableListOf<TaskEntity>()

            filteredTasks.forEach { task ->
                // debug
                //println("Current task: id=${task.id}, name=${task.name}, dueDate=${task.dueDate}, isCompleted=${task.isCompleted}")
                if (task.isCompleted) {
                    completedTasks.add(task)
                } else {
                    try {
                        val dueDate = dateFormat.parse(task.dueDate)
                        if (dueDate != null) {
                            // Set both dueDate and currentDate to 00:00:00 of the current day, then compare.
                            val normDueDate = Calendar.getInstance().apply {
                                time = dueDate
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.time

                            val normCurrentDate = Calendar.getInstance().apply {
                                time = currentDate
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.time

                            // What is being compared now is the pure date (excluding time).
                            if (normDueDate.before(normCurrentDate)) {
                                // Deadline is earlier than today → overdue
                                overdueTasks.add(task)
                            } else {
                                // The deadline is today or later → Not overdue
                                upcomingTasks.add(task)
                            }
                        }
                    } catch (e: Exception) {
                        println("Parsing task date error: ${task.name}, dueDate: ${task.dueDate}, error: ${e.message}")
                    }
                }
            }

            listOf(
                TaskStatusSummaryEntry(
                    statusName = "Upcoming Tasks",
                    totalTasks = upcomingTasks.size,
                    importantTasks = upcomingTasks.count { it.isImportant }
                ),
                TaskStatusSummaryEntry(
                    statusName = "Overdue Tasks",
                    totalTasks = overdueTasks.size,
                    importantTasks = overdueTasks.count { it.isImportant }
                ),
                TaskStatusSummaryEntry(
                    statusName = "Completed Tasks",
                    totalTasks = completedTasks.size,
                    importantTasks = completedTasks.count { it.isImportant }
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val completionPercentage: StateFlow<Float> = repository.allTasks
        .combine(_selectedDateFilter) { tasks, filter ->
            val currentDate = Calendar.getInstance().time
            val relevantTasks = tasks.filter { task ->
                if (filter == DateFilter.ALL) {
                    true
                } else {
                    try {
                        val dueDate = dateFormat.parse(task.dueDate)
                        if (dueDate != null) {
                            val calendar = Calendar.getInstance()
                            calendar.time = currentDate
                            
                            val startDate = when (filter) {
                                DateFilter.LAST_1_WEEK -> {
                                    calendar.add(Calendar.WEEK_OF_YEAR, -1)
                                    calendar.time
                                }
                                DateFilter.LAST_2_WEEKS -> {
                                    calendar.add(Calendar.WEEK_OF_YEAR, -2)
                                    calendar.time
                                }
                                else -> Date(0) // Earliest possible date
                            }
                            
                            !dueDate.before(startDate) && !dueDate.after(currentDate)
                        } else {
                            false
                        }
                    } catch (e: Exception) {
                        false
                    }
                }
            }

            if (relevantTasks.isEmpty()) {
                0f
            } else {
                val completedCount = relevantTasks.count { it.isCompleted }
                (completedCount.toFloat() / relevantTasks.size.toFloat()) * 100
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0f
        )
} 