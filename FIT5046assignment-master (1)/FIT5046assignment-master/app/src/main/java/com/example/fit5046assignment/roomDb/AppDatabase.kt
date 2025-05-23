package com.example.fit5046assignment.roomDb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The main database class for the application, built using Room Persistence Library.
 * This class defines the database configuration, including the entities it contains and its version.
 * It also provides access to the DAOs (Data Access Objects).
 *
 * @property entities An array of all entity classes that are part of this database.
 * @property version The version number of the database. Increment this when the schema changes.
 * @property exportSchema If true, Room exports the database schema into a JSON file in the project.
 *                      It's generally good practice to version control this schema.
 */
@Database(entities = [TaskEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Abstract method to get an instance of [TaskDao].
     * Room will generate the implementation for this method.
     * @return An instance of [TaskDao] for accessing task data.
     */
    abstract fun taskDao(): TaskDao

    /**
     * Abstract method to get an instance of [TaskModificationDao].
     * Room will generate the implementation for this method.
     * @return An instance of [TaskModificationDao] for specific task modification operations.
     */
    abstract fun taskModificationDao(): TaskModificationDao

    /**
     * Companion object to provide a singleton instance of the database.
     * This ensures that only one instance of the database is created throughout the app's lifecycle,
     * preventing potential issues with multiple open database connections.
     */
    companion object {
        /**
         * Marks the INSTANCE variable as volatile to ensure that writes to this field
         * are immediately visible to other threads.
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton instance of the [AppDatabase].
         * If an instance does not exist, it creates one in a thread-safe manner.
         *
         * @param context The application context, used to get the path to the database file.
         * @return The singleton [AppDatabase] instance.
         */
        fun getDatabase(context: Context): AppDatabase {
            // Return the existing instance if it's not null.
            // If it is null, enter a synchronized block to ensure thread safety during creation.
            return INSTANCE ?: synchronized(this) {
                // Build the database instance using Room's databaseBuilder.
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Use application context to avoid memory leaks.
                    AppDatabase::class.java,    // The database class.
                    "task_database"       // The name of the database file.
                )
                    // Specifies a migration strategy for when the database version is upgraded.
                    // fallbackToDestructiveMigration will recreate the database if no migration path is provided,
                    // meaning all existing data will be lost. Use carefully in production.
                    .fallbackToDestructiveMigration()
                    // Adds a callback to the database build process.
                    .addCallback(AppDatabaseCallback(context))
                    .build()
                INSTANCE = instance // Assign the newly created instance to INSTANCE.
                instance // Return the instance.
            }
        }
    }

    /**
     * A private inner class that extends [RoomDatabase.Callback].
     * This callback is used to perform actions when the database is created or opened.
     *
     * @param context The application context, passed through to be potentially used by the callback methods.
     */
    private class AppDatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {

        /**
         * Called when the database is created for the first time, after the tables have been created.
         * This is where initial data population can occur.
         *
         * @param db The database instance.
         */
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // If the database instance is available, launch a coroutine to populate it.
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.taskDao())
                }
            }
        }

        /**
         * Populates the database with initial sample task data.
         * This method is called from [onCreate] when the database is first created.
         * It inserts a predefined set of tasks into the "tasks" table.
         * All database operations here are suspend functions and are executed on an IO dispatcher.
         *
         * @param taskDao The Data Access Object for tasks, used to insert the sample data.
         */
        suspend fun populateDatabase(taskDao: TaskDao) {
            // Define due dates to be used for sample tasks.
            val upcomingDueDate = "May 16, 2025"
            val overdueDueDate = "May 5, 2025"
            val completedDueDate = "May 10, 2025"

            // Base timestamp for generating unique creation dates.
            var currentMillis = System.currentTimeMillis()

            /**
             * Helper function to generate unique creation timestamps as strings.
             * Each call subtracts a random interval (1-5 seconds) from currentMillis
             * to ensure distinct and chronologically ordered creation times for sample data.
             * @return A string representation of a millisecond timestamp.
             */
            fun getUniqueTimestamp(): String {
                currentMillis -= (1000..5000).random() // Subtract 1-5 seconds
                return currentMillis.toString()
            }

            // --- Sample Data Insertion ---
            // The following blocks insert various sample tasks with different attributes.
            // Each task gets a unique creation timestamp generated by getUniqueTimestamp().

            // Upcoming Tasks - 5 Important, 10 Normal
            taskDao.insertTask(TaskEntity(name = "Upcoming Important Task 1", creationDate = getUniqueTimestamp(), dueDate = upcomingDueDate, isImportant = true, isCompleted = false))
            taskDao.insertTask(TaskEntity(name = "Upcoming Important Task 2", creationDate = getUniqueTimestamp(), dueDate = upcomingDueDate, isImportant = true, isCompleted = false))
            taskDao.insertTask(TaskEntity(name = "Upcoming Important Task 3", creationDate = getUniqueTimestamp(), dueDate = upcomingDueDate, isImportant = true, isCompleted = false))
            taskDao.insertTask(TaskEntity(name = "Upcoming Important Task 4", creationDate = getUniqueTimestamp(), dueDate = upcomingDueDate, isImportant = true, isCompleted = false))
            taskDao.insertTask(TaskEntity(name = "Upcoming Important Task 5", creationDate = getUniqueTimestamp(), dueDate = upcomingDueDate, isImportant = true, isCompleted = false))

            taskDao.insertTask(TaskEntity(name = "Upcoming Task 1", creationDate = getUniqueTimestamp(), dueDate = upcomingDueDate, isImportant = false, isCompleted = false))
            taskDao.insertTask(TaskEntity(name = "Upcoming Task 2", creationDate = getUniqueTimestamp(), dueDate = upcomingDueDate, isImportant = false, isCompleted = false))
            taskDao.insertTask(TaskEntity(name = "Upcoming Task 3", creationDate = getUniqueTimestamp(), dueDate = upcomingDueDate, isImportant = false, isCompleted = false))
            taskDao.insertTask(TaskEntity(name = "Upcoming Task 4", creationDate = getUniqueTimestamp(), dueDate = upcomingDueDate, isImportant = false, isCompleted = false))
            taskDao.insertTask(TaskEntity(name = "Upcoming Task 5", creationDate = getUniqueTimestamp(), dueDate = upcomingDueDate, isImportant = false, isCompleted = false))
            taskDao.insertTask(TaskEntity(name = "Upcoming Task 6", creationDate = getUniqueTimestamp(), dueDate = upcomingDueDate, isImportant = false, isCompleted = false))
            taskDao.insertTask(TaskEntity(name = "Upcoming Task 7", creationDate = getUniqueTimestamp(), dueDate = upcomingDueDate, isImportant = false, isCompleted = false))
            taskDao.insertTask(TaskEntity(name = "Upcoming Task 8", creationDate = getUniqueTimestamp(), dueDate = upcomingDueDate, isImportant = false, isCompleted = false))
            taskDao.insertTask(TaskEntity(name = "Upcoming Task 9", creationDate = getUniqueTimestamp(), dueDate = upcomingDueDate, isImportant = false, isCompleted = false))
            taskDao.insertTask(TaskEntity(name = "Upcoming Task 10", creationDate = getUniqueTimestamp(), dueDate = upcomingDueDate, isImportant = false, isCompleted = false))

            // Overdue Tasks - 3 Important, 5 Normal
            taskDao.insertTask(TaskEntity(name = "Overdue Important Task 1", creationDate = getUniqueTimestamp(), dueDate = overdueDueDate, isImportant = true, isCompleted = false))
            taskDao.insertTask(TaskEntity(name = "Overdue Important Task 2", creationDate = getUniqueTimestamp(), dueDate = overdueDueDate, isImportant = true, isCompleted = false))
            taskDao.insertTask(TaskEntity(name = "Overdue Important Task 3", creationDate = getUniqueTimestamp(), dueDate = overdueDueDate, isImportant = true, isCompleted = false))

//            taskDao.insertTask(TaskEntity(name = "Overdue Task 1", creationDate = getUniqueTimestamp(), dueDate = overdueDueDate, isImportant = false, isCompleted = false))
//            taskDao.insertTask(TaskEntity(name = "Overdue Task 2", creationDate = getUniqueTimestamp(), dueDate = overdueDueDate, isImportant = false, isCompleted = false))
//            taskDao.insertTask(TaskEntity(name = "Overdue Task 3", creationDate = getUniqueTimestamp(), dueDate = overdueDueDate, isImportant = false, isCompleted = false))
//            taskDao.insertTask(TaskEntity(name = "Overdue Task 4", creationDate = getUniqueTimestamp(), dueDate = overdueDueDate, isImportant = false, isCompleted = false))
//            taskDao.insertTask(TaskEntity(name = "Overdue Task 5", creationDate = getUniqueTimestamp(), dueDate = overdueDueDate, isImportant = false, isCompleted = false))

            // Completed Tasks - 2 Important, 10 Normal
            taskDao.insertTask(TaskEntity(name = "Completed Important Task 1", creationDate = getUniqueTimestamp(), dueDate = completedDueDate, isImportant = true, isCompleted = true))
            taskDao.insertTask(TaskEntity(name = "Completed Important Task 2", creationDate = getUniqueTimestamp(), dueDate = completedDueDate, isImportant = true, isCompleted = true))

//            taskDao.insertTask(TaskEntity(name = "Completed Task 1", creationDate = getUniqueTimestamp(), dueDate = completedDueDate, isImportant = false, isCompleted = true))
//            taskDao.insertTask(TaskEntity(name = "Completed Task 2", creationDate = getUniqueTimestamp(), dueDate = completedDueDate, isImportant = false, isCompleted = true))
//            taskDao.insertTask(TaskEntity(name = "Completed Task 3", creationDate = getUniqueTimestamp(), dueDate = completedDueDate, isImportant = false, isCompleted = true))
//            taskDao.insertTask(TaskEntity(name = "Completed Task 4", creationDate = getUniqueTimestamp(), dueDate = completedDueDate, isImportant = false, isCompleted = true))
//            taskDao.insertTask(TaskEntity(name = "Completed Task 5", creationDate = getUniqueTimestamp(), dueDate = completedDueDate, isImportant = false, isCompleted = true))
//            taskDao.insertTask(TaskEntity(name = "Completed Task 6", creationDate = getUniqueTimestamp(), dueDate = completedDueDate, isImportant = false, isCompleted = true))
//            taskDao.insertTask(TaskEntity(name = "Completed Task 7", creationDate = getUniqueTimestamp(), dueDate = completedDueDate, isImportant = false, isCompleted = true))
//            taskDao.insertTask(TaskEntity(name = "Completed Task 8", creationDate = getUniqueTimestamp(), dueDate = completedDueDate, isImportant = false, isCompleted = true))
//            taskDao.insertTask(TaskEntity(name = "Completed Task 9", creationDate = getUniqueTimestamp(), dueDate = completedDueDate, isImportant = false, isCompleted = true))
//            taskDao.insertTask(TaskEntity(name = "Completed Task 10", creationDate = getUniqueTimestamp(), dueDate = completedDueDate, isImportant = false, isCompleted = true))
        }
    }
}