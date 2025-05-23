package com.example.fit5046assignment.roomDb

import androidx.room.Dao
import androidx.room.Query

@Dao
interface TaskModificationDao {

    /**
     * Updates the due date of a specific task.
     * @param taskId The ID of the task to update.
     * @param newDueDate The new due date string.
     */
    @Query("UPDATE tasks SET dueDate = :newDueDate WHERE id = :taskId")
    suspend fun updateDueDate(taskId: Int, newDueDate: String)

    /**
     * Updates the importance status of a specific task.
     * @param taskId The ID of the task to update.
     * @param newImportance The new importance status (true or false).
     */
    @Query("UPDATE tasks SET isImportant = :newImportance WHERE id = :taskId")
    suspend fun updateImportance(taskId: Int, newImportance: Boolean)

    /**
     * Deletes a specific task by its ID.
     * @param taskId The ID of the task to delete.
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)
} 