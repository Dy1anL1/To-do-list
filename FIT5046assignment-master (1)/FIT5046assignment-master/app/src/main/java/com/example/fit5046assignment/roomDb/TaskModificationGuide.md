# Guide: Modifying and Deleting Tasks

This guide explains how to use the provided functions to modify the due date and importance of existing tasks, or to delete tasks from the database. These operations are managed through the `TaskRepository`.

## Overview

The system allows for the following modifications to individual tasks:
-   Updating the due date.
-   Changing the importance status (marking as important or not important).
-   Deleting a task.

**Important Note:** The `creationDate` of a task is immutable and cannot be changed using these functions.

## Accessing Modification Functions

All task modification and deletion operations are exposed as `suspend` functions within the `TaskRepository`. You will need an instance of `TaskRepository` (typically injected into your ViewModel) to call these methods.

## Available Functions

The following functions are available in `TaskRepository.kt`:

1.  **Update Task Due Date**
    ```kotlin
    suspend fun updateTaskDueDate(taskId: Int, newDueDate: String)
    ```
    -   **Purpose:** Changes the due date of a specific task.
    -   **Parameters:**
        -   `taskId: Int`: The unique ID of the task you want to modify.
        -   `newDueDate: String`: The new due date for the task (e.g., "YYYY-MM-DD").
    -   **Usage Example (from a ViewModel):**
        ```kotlin
        viewModelScope.launch {
            repository.updateTaskDueDate(taskId = 123, newDueDate = "2025-01-15")
        }
        ```

2.  **Update Task Importance**
    ```kotlin
    suspend fun updateTaskImportance(taskId: Int, newImportance: Boolean)
    ```
    -   **Purpose:** Toggles the importance status of a specific task.
    -   **Parameters:**
        -   `taskId: Int`: The unique ID of the task you want to modify.
        -   `newImportance: Boolean`: The new importance status (`true` for important, `false` for not important).
    -   **Usage Example (from a ViewModel):**
        ```kotlin
        viewModelScope.launch {
            // Mark task 456 as important
            repository.updateTaskImportance(taskId = 456, newImportance = true)
            
            // Mark task 789 as not important
            repository.updateTaskImportance(taskId = 789, newImportance = false)
        }
        ```

3.  **Delete Task by ID**
    ```kotlin
    suspend fun deleteTaskById(taskId: Int)
    ```
    -   **Purpose:** Permanently removes a specific task from the database.
    -   **Parameters:**
        -   `taskId: Int`: The unique ID of the task you want to delete.
    -   **Usage Example (from a ViewModel):**
        ```kotlin
        viewModelScope.launch {
            repository.deleteTaskById(taskId = 101)
        }
        ```

## Important Considerations

-   **Coroutine Scope:** All these functions are `suspend` functions. They must be called from a coroutine (e.g., using `viewModelScope.launch` in a ViewModel, or another appropriate `CoroutineScope`).
-   **Task ID:** You need to know the `id` of the `TaskEntity` to use these modification functions. This ID is auto-generated by Room when a task is first inserted.
-   **Error Handling:** Consider adding error handling (e.g., try-catch blocks) around these calls in your ViewModel or service layer if needed, although Room handles many database-level errors internally.
-   **Data Consistency:** After performing an update or delete, the `Flow<List<TaskEntity>>` observed from `repository.allTasks` will automatically emit the new list of tasks, allowing your UI to update reactively.

This guide should help developers effectively use the task modification features. 

---

# 指南：修改与删除任务

本指南解释了如何使用提供的函数来修改现有任务的截止日期和重要性，或从数据库中删除任务。这些操作通过 `TaskRepository` 进行管理。

## 概述

系统允许对单个任务进行以下修改：
-   更新截止日期。
-   更改重要性状态（标记为重要或不重要）。
-   删除任务。

**重要提示：** 任务的 `creationDate`（创建日期）是不可变的，无法使用这些函数进行更改。

## 访问修改函数

所有任务修改和删除操作都在 `TaskRepository` 中以 `suspend` 函数的形式公开。您将需要一个 `TaskRepository` 的实例（通常注入到您的 ViewModel 中）来调用这些方法。

## 可用函数

`TaskRepository.kt` 中提供了以下函数：

1.  **更新任务截止日期**
    ```kotlin
    suspend fun updateTaskDueDate(taskId: Int, newDueDate: String)
    ```
    -   **目的：** 更改特定任务的截止日期。
    -   **参数：**
        -   `taskId: Int`：您要修改的任务的唯一 ID。
        -   `newDueDate: String`：任务的新截止日期（例如 "YYYY-MM-DD"）。
    -   **用法示例（在 ViewModel 中）：**
        ```kotlin
        viewModelScope.launch {
            repository.updateTaskDueDate(taskId = 123, newDueDate = "2025-01-15")
        }
        ```

2.  **更新任务重要性**
    ```kotlin
    suspend fun updateTaskImportance(taskId: Int, newImportance: Boolean)
    ```
    -   **目的：** 切换特定任务的重要性状态。
    -   **参数：**
        -   `taskId: Int`：您要修改的任务的唯一 ID。
        -   `newImportance: Boolean`：新的重要性状态（`true` 表示重要，`false` 表示不重要）。
    -   **用法示例（在 ViewModel 中）：**
        ```kotlin
        viewModelScope.launch {
            // 将任务 456 标记为重要
            repository.updateTaskImportance(taskId = 456, newImportance = true)
            
            // 将任务 789 标记为不重要
            repository.updateTaskImportance(taskId = 789, newImportance = false)
        }
        ```

3.  **通过 ID 删除任务**
    ```kotlin
    suspend fun deleteTaskById(taskId: Int)
    ```
    -   **目的：** 从数据库中永久删除特定任务。
    -   **参数：**
        -   `taskId: Int`：您要删除的任务的唯一 ID。
    -   **用法示例（在 ViewModel 中）：**
        ```kotlin
        viewModelScope.launch {
            repository.deleteTaskById(taskId = 101)
        }
        ```

## 重要注意事项

-   **协程作用域：** 所有这些函数都是 `suspend` 函数。它们必须从协程中调用（例如，在 ViewModel 中使用 `viewModelScope.launch`，或另一个适当的 `CoroutineScope`）。
-   **任务 ID：** 您需要知道 `TaskEntity` 的 `id` 才能使用这些修改函数。此 ID 在任务首次插入时由 Room 自动生成。
-   **错误处理：** 如果需要，可以考虑在 ViewModel 或服务层中的这些调用周围添加错误处理（例如 try-catch 块），尽管 Room 内部会处理许多数据库级别的错误。
-   **数据一致性：** 执行更新或删除后，从 `repository.allTasks` 观察到的 `Flow<List<TaskEntity>>` 将自动发出新的任务列表，从而使您的 UI 能够响应式更新。

本指南应能帮助开发人员有效地使用任务修改功能。 