package com.example.fit5046assignment.roomDb

import android.app.Application
import kotlinx.coroutines.flow.Flow

/**
 * Repository class that abstracts access to multiple data sources, primarily the Room database via DAOs.
 * It provides a clean API for data access to the rest of the application, like ViewModels.
 * This class manages querying and allows use of different backend (though here only Room is used).
 *
 * @param application The application context, used to get the database instance.
 */
class TaskRepository(application: Application) {

    // Instance of the general TaskDao for common CRUD operations.
    private val taskDao: TaskDao = AppDatabase.getDatabase(application).taskDao()
    // Instance of TaskModificationDao for specific field updates and deletions by ID.
    private val taskModificationDao: TaskModificationDao = AppDatabase.getDatabase(application).taskModificationDao()

    /**
     * A [Flow] that emits a list of all [TaskEntity] objects from the database.
     * This Flow is collected by ViewModels to observe data changes reactively.
     * The data is ordered by completion status and importance as defined in [TaskDao.getAllTasks].
     */
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    /**
     * Inserts a new task into the database.
     * This is a suspend function and delegates to [TaskDao.insertTask].
     * @param task The [TaskEntity] to insert.
     */
    suspend fun insert(task: TaskEntity) = taskDao.insertTask(task)

    /**
     * Updates an existing task in the database.
     * This method updates the entire [TaskEntity] object.
     * This is a suspend function and delegates to [TaskDao.updateTask].
     * @param task The [TaskEntity] to update. Its `id` must match an existing task.
     */
    suspend fun update(task: TaskEntity) = taskDao.updateTask(task)

    /**
     * Deletes a specified task from the database.
     * This method requires the entire [TaskEntity] object to identify the task for deletion.
     * This is a suspend function and delegates to [TaskDao.deleteTask].
     * @param task The [TaskEntity] to delete.
     */
    suspend fun delete(task: TaskEntity) = taskDao.deleteTask(task)

    // --- Methods using TaskModificationDao for more granular operations ---

    /**
     * Updates only the due date of a specific task, identified by its ID.
     * This is a suspend function and delegates to [TaskModificationDao.updateDueDate].
     * @param taskId The ID of the task to update.
     * @param newDueDate The new due date string (e.g., "YYYY-MM-DD").
     */
    suspend fun updateTaskDueDate(taskId: Int, newDueDate: String) {
        taskModificationDao.updateDueDate(taskId, newDueDate)
    }

    /**
     * Updates only the importance status of a specific task, identified by its ID.
     * This is a suspend function and delegates to [TaskModificationDao.updateImportance].
     * @param taskId The ID of the task to update.
     * @param newImportance The new importance status (`true` if important, `false` otherwise).
     */
    suspend fun updateTaskImportance(taskId: Int, newImportance: Boolean) {
        taskModificationDao.updateImportance(taskId, newImportance)
    }

    /**
     * Deletes a task from the database, identified by its ID.
     * This is a suspend function and delegates to [TaskModificationDao.deleteTaskById].
     * @param taskId The ID of the task to delete.
     */
    suspend fun deleteTaskById(taskId: Int) {
        taskModificationDao.deleteTaskById(taskId)
    }
}