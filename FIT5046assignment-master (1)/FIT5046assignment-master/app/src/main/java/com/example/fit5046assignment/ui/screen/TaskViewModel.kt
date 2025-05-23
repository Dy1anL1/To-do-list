package com.example.fit5046assignment.ui.screen

import android.app.Application
import android.content.Context
import android.content.ContentValues
import android.provider.CalendarContract
import android.provider.CalendarContract.Calendars
import android.provider.CalendarContract.Events
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fit5046assignment.roomDb.TaskEntity
import com.example.fit5046assignment.roomDb.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.text.ParseException
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.Date
import java.util.TimeZone


class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository = TaskRepository(application)
    private val appContext = getApplication<Application>().applicationContext

    val allTasks: Flow<List<TaskEntity>> = repository.allTasks

    // insert a new task into database and calendar
    fun insertTask(task: TaskEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(task)
        syncTaskToCalendar(
            context    = appContext,
            title      = task.name,
            description= null,
            dueDateStr = task.dueDate,
            isImportant= task.isImportant
        )
    }

    // update a task in database (you can add calendar-update logic here if needed)
    fun updateTask(task: TaskEntity) = viewModelScope.launch(Dispatchers.IO) {
        // Update in local database
        repository.update(task)

        // If importance toggled, remove old calendar event and add new one with star prefix
        val oldImportant = !task.isImportant
        deleteTaskFromCalendar(
            context    = appContext,
            title      = task.name,
            dueDateStr = task.dueDate,
            isImportant= oldImportant
        )
        syncTaskToCalendar(
            context    = appContext,
            title      = task.name,
            description= null,
            dueDateStr = task.dueDate,
            isImportant= task.isImportant
        )
    }

    // Delete a task from both database and calendar
    fun deleteTask(task: TaskEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(task)
        deleteTaskFromCalendar(
            context    = appContext,
            title      = task.name,
            dueDateStr = task.dueDate,
            isImportant= task.isImportant
        )
    }

    fun updateOverdueStatus() = viewModelScope.launch(Dispatchers.IO) {
        val currentDateStr = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            .format(Calendar.getInstance().time)

        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

        val currentDate = dateFormat.parse(currentDateStr) ?: Calendar.getInstance().time

        repository.allTasks.collect { tasks ->
            tasks.forEach { task ->
                try {
                    val dueDate = dateFormat.parse(task.dueDate)
                    
                    // Compare dates ignoring time components
                    // Get calendar instances for comparison with just the date portion
                    val dueCal = Calendar.getInstance().apply {
                        time = dueDate ?: Date()
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    
                    val currentCal = Calendar.getInstance().apply {
                        time = currentDate
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    
                    // A task is overdue only if its due date is strictly BEFORE today
                    // (not including today's tasks)
                    val shouldBeOverdue = !task.isCompleted && dueCal.before(currentCal)
                    
                    if (task.isOverdue != shouldBeOverdue) {
                        repository.update(task.copy(isOverdue = shouldBeOverdue))
                    }
                } catch (e: Exception) {
                    // Handle parsing errors - log or update format if needed
                    println("Error parsing date: ${task.dueDate}")
                }
            }

            // Collect only once and exit
            return@collect
        }
    }

    val importantTasks: Flow<List<TaskEntity>> =
        repository.allTasks.map { it.filter(TaskEntity::isImportant) }

    // Check task status
    fun updateTaskCompletionStatus(task: TaskEntity, isCompleted: Boolean) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = isCompleted)
            repository.update(updatedTask)
        }
    }

    /**
     * Add an all-day event to the user's primary calendar.
     * @param context Android context
     * @param title Event title (prefix with star if important)
     * @param description Optional event description
     * @param dueDateStr Date string like "May 23, 2025" or localized Chinese
     * @param isImportant Whether this task is marked important
     */
    private fun syncTaskToCalendar(
        context: Context,
        title: String,
        description: String?,
        dueDateStr: String,
        isImportant: Boolean
    ) {
        Log.d("CalendarSync", "syncTaskToCalendar called for: '$title' important=$isImportant on '$dueDateStr'")

        // 1. Check read/write calendar permissions
        val writeOk = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) ==
                PackageManager.PERMISSION_GRANTED
        val readOk = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
                PackageManager.PERMISSION_GRANTED
        if (!writeOk || !readOk) {
            Log.w("CalendarSync", "Calendar permissions missing, skip sync")
            return
        }

        // 2. Find a visible or primary calendar ID
        var calId: Long? = null
        context.contentResolver.query(
            Calendars.CONTENT_URI,
            arrayOf(Calendars._ID, Calendars.IS_PRIMARY),
            "${Calendars.VISIBLE}=1",
            null,
            null
        )?.use { cursor ->
            val idxId = cursor.getColumnIndexOrThrow(Calendars._ID)
            val idxPrimary = cursor.getColumnIndexOrThrow(Calendars.IS_PRIMARY)
            while (cursor.moveToNext()) {
                if (cursor.getInt(idxPrimary) == 1) {
                    calId = cursor.getLong(idxId)
                    break
                }
            }
        }
        if (calId == null) {
            // fallback to first visible calendar
            context.contentResolver.query(
                Calendars.CONTENT_URI,
                arrayOf(Calendars._ID),
                "${Calendars.VISIBLE}=1",
                null,
                null
            )?.use { cursor2 ->
                if (cursor2.moveToFirst()) {
                    calId = cursor2.getLong(cursor2.getColumnIndexOrThrow(Calendars._ID))
                }
            }
        }
        if (calId == null) {
            Log.e("CalendarSync", "No calendar found, abort sync")
            return
        }

        // 3. Parse date with explicit UTC timezone for consistency
        val utcTimeZone = TimeZone.getTimeZone("UTC")
        val fmtZh = SimpleDateFormat("MMMM d, yyyy", Locale.CHINESE).apply {
            timeZone = utcTimeZone
        }
        val fmtEn = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).apply {
            timeZone = utcTimeZone
        }

        val parsedDate: Date = try {
            fmtZh.parse(dueDateStr)!!
        } catch (_: ParseException) {
            try {
                fmtEn.parse(dueDateStr)!!
            } catch (_: ParseException) {
                Log.e("CalendarSync", "Unable to parse date '$dueDateStr', skipping sync")
                return
            }
        }

        // 4. Calculate UTC midnight start and end times
        val cal = Calendar.getInstance(utcTimeZone).apply {
            time = parsedDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dtStartUtc = cal.timeInMillis
        val dtEndUtc = dtStartUtc + 24L * 60 * 60 * 1000

        Log.d("CalendarSync", "Creating all-day event for '$dueDateStr' - UTC start: $dtStartUtc, end: $dtEndUtc")

        // 5. Prepare event title with star prefix if important
        val eventTitle = if (isImportant) "⭐ $title" else title

        // 6. Insert all-day event using UTC timezone
        try {
            val values = ContentValues().apply {
                put(Events.DTSTART, dtStartUtc)
                put(Events.DTEND, dtEndUtc)
                put(Events.ALL_DAY, 1)
                put(Events.TITLE, eventTitle)
                put(Events.DESCRIPTION, description ?: "")
                put(Events.CALENDAR_ID, calId)
                // Use UTC timezone for all-day events to avoid timezone issues
                put(Events.EVENT_TIMEZONE, "UTC")
            }
            val uri = context.contentResolver.insert(Events.CONTENT_URI, values)
            if (uri != null) {
                Log.d("CalendarSync", "All-day event created: $uri")
                context.contentResolver.notifyChange(Events.CONTENT_URI, null)
            } else {
                Log.e("CalendarSync", "Insert returned null URI")
            }
        } catch (sec: SecurityException) {
            Log.e("CalendarSync", "SecurityException: ${sec.message}")
        } catch (e: Exception) {
            Log.e("CalendarSync", "Error inserting event", e)
        }
    }


    /**
     * Delete matching all-day event from calendar.
     * Match by title, ALL_DAY flag and DTSTART within that day.
     */
    private fun deleteTaskFromCalendar(
        context: Context,
        title: String,
        dueDateStr: String,
        isImportant: Boolean
    ) {
        val tag = "CalendarSync"

        // Parse date with explicit UTC timezone for consistency
        val utcTimeZone = TimeZone.getTimeZone("UTC")
        val fmtEn = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).apply {
            timeZone = utcTimeZone
        }
        val fmtZh = SimpleDateFormat("MMMM d, yyyy", Locale.CHINESE).apply {
            timeZone = utcTimeZone
        }
        
        val parsedDate = try {
            fmtEn.parse(dueDateStr)
        } catch (_: ParseException) {
            try { fmtZh.parse(dueDateStr) } catch (_: Exception) { null }
        } ?: run {
            Log.w(tag, "Failed to parse delete date: $dueDateStr")
            return
        }

        // Compute UTC midnight range - same logic as creation
        val cal = Calendar.getInstance(utcTimeZone).apply {
            time = parsedDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startUtc = cal.timeInMillis
        val endUtc = startUtc + 24L * 60 * 60 * 1000

        val eventTitle = if (isImportant) "⭐ $title" else title

        Log.d(tag, "Deleting event '$eventTitle' for '$dueDateStr' - UTC range: $startUtc to $endUtc")

        // Build selection and args
        val selection = """
            ${CalendarContract.Events.ALL_DAY}=1
            AND ${CalendarContract.Events.TITLE}=?
            AND ${CalendarContract.Events.DTSTART}>=?
            AND ${CalendarContract.Events.DTSTART}<?
        """.trimIndent()
        val args = arrayOf(eventTitle, startUtc.toString(), endUtc.toString())

        // Perform deletion
        try {
            val rowsDeleted = context.contentResolver.delete(
                CalendarContract.Events.CONTENT_URI, selection, args
            )
            Log.d(tag, "Deleted $rowsDeleted calendar event(s) for '$eventTitle' on '$dueDateStr'")
        } catch (e: Exception) {
            Log.e(tag, "Failed to delete calendar event", e)
        }
    }
}