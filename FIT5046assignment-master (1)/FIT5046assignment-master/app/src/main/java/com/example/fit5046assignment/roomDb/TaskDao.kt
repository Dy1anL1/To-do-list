package com.example.fit5046assignment.roomDb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the [TaskEntity] table.
 * This interface defines all the database operations (queries, inserts, updates, deletes)
 * that can be performed on the "tasks" table. Room will generate the implementation for these methods.
 */
@Dao
interface TaskDao {

    /**
     * Retrieves all tasks from the "tasks" table, ordered by completion status (incomplete first)
     * and then by importance (important first within each completion status group).
     * @return A [Flow] that emits a list of [TaskEntity] objects. The Flow allows for reactive updates,
     *         meaning the UI or other observers will be notified automatically when the data changes.
     */
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, isImportant DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    /**
     * Inserts a new task into the database. If a task with the same primary key already exists,
     * it will be replaced due to the [OnConflictStrategy.REPLACE] strategy.
     * This is a suspend function, so it must be called from a coroutine or another suspend function.
     * @param task The [TaskEntity] object to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    /**
     * Updates an existing task in the database. The task is identified by its primary key (`id`).
     * This is a suspend function.
     * @param task The [TaskEntity] object to update. Its `id` field must match an existing task.
     */
    @Update
    suspend fun updateTask(task: TaskEntity)

    /**
     * Deletes a specified task from the database. The task is identified by its primary key.
     * This is a suspend function.
     * @param task The [TaskEntity] object to delete.
     */
    @Delete
    suspend fun deleteTask(task: TaskEntity)

    /**
     * Deletes all tasks from the "tasks" table.
     * Use with caution as this will permanently remove all task data.
     * This is a suspend function.
     */
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}