# Report Screen Data Types

This document details the data structures and types required by the `ReportScreen.kt` composable to display user reports, including a task status summary pie chart and a produce sales line chart.

## 1. Task Status Summary Pie Chart

This pie chart visualises a summary of tasks, categorised by status (e.g., Upcoming, Overdue). It also provides a breakdown of how many tasks within each category are marked as important. This data would typically be aggregated from the "tasks" table in the Room database.

**Data Structure:**
The chart expects a list of `TaskStatusSummaryEntry` objects.

```kotlin
data class TaskStatusSummaryEntry(
    val statusName: String,        // e.g., "Upcoming Tasks", "Overdue Tasks"
    val totalTasks: Int,           // Total number of tasks for this status
    val importantTasks: Int,       // Number of important tasks included in totalTasks
    // val color: Color            // Optional: if you want to specify colours per category (e.g., from androidx.compose.ui.graphics.Color)
)
```

**Example Usage in Composable (Conceptual - for data passing):**
```kotlin
// Sample Data
val taskSummaryData: List<TaskStatusSummaryEntry> = listOf(
    TaskStatusSummaryEntry(statusName = "Upcoming Tasks", totalTasks = 15, importantTasks = 5),
    TaskStatusSummaryEntry(statusName = "Overdue Tasks",  totalTasks = 8,  importantTasks = 3),
    TaskStatusSummaryEntry(statusName = "Completed Today", totalTasks = 12, importantTasks = 2) // Example of another category
)

// This data would be passed to a Composable responsible for rendering the pie chart.
// e.g., TaskStatusPieChart(data = taskSummaryData)

// The pie chart slices would typically represent the 'totalTasks' for each 'statusName'.
// The 'importantTasks' count is additional information for each slice, which could be shown
// in a legend, tooltip, or as part of the slice label when a real charting library is used.
```

**Data Source (Conceptual - from Room Database):**
To populate this, you would query your "tasks" table. For example:
*   **Upcoming Tasks:** Count tasks where `dueDate` is in the future and `completionStatus` is false.
*   **Overdue Tasks:** Count tasks where `dueDate` is in the past and `completionStatus` is false.
*   **Important Tasks:** For each category, count tasks where `priority` (or a dedicated 'isImportant' field) indicates high importance.

## 2. Produce Sales Line Chart

This chart displays the trend of produce sales over a period, typically monthly. The labels "Produce sales" and month indicators (e.g., "OCT", "NOV") are based on the UI image.

**Data Structure:**
The chart expects a list of `ProduceSalesDataPoint` objects.

```kotlin
data class ProduceSalesDataPoint(
    val month: String,     // e.g., "OCT", "NOV", "DEC"
    val salesValue: Float  // e.g., sales amount or quantity
)
```

**Example Usage in Composable (Conceptual):**
```kotlin
// Sample Data
val produceSalesData: List<ProduceSalesDataPoint> = listOf(
    ProduceSalesDataPoint(month = "OCT", salesValue = 20f), // Values are illustrative
    ProduceSalesDataPoint(month = "NOV", salesValue = 40f),
    ProduceSalesDataPoint(month = "DEC", salesValue = 25f),
    ProduceSalesDataPoint(month = "JAN", salesValue = 28f),
    ProduceSalesDataPoint(month = "FEB", salesValue = 35f),
    ProduceSalesDataPoint(month = "MAR", salesValue = 18f),
    ProduceSalesDataPoint(month = "APR", salesValue = 22f),
    ProduceSalesDataPoint(month = "MAY", salesValue = 38f),
    ProduceSalesDataPoint(month = "JUN", salesValue = 45f)
)

// Pass this data to a composable that renders the line chart.
// e.g., LineChartComposable(data = produceSalesData)
```

## 3. Advice Button

The "Advice" button currently triggers an `onAdviceClick()` function.
Future enhancements could involve this function using the displayed report data (e.g., `personalAccountingData`, `produceSalesData`) to generate contextual advice. The data passed to the `ReportScreen` would then also be available for this advice generation logic.

```kotlin
// Current function signature in ReportScreen.kt
// fun onAdviceClick() { TODO("Not yet implemented") }

// Potential future signature if advice needs data:
// fun onAdviceClick(
//     accountingData: List<PersonalAccountingEntry>,
//     salesData: List<ProduceSalesDataPoint>
// ) { /* ... logic to generate advice ... */ }
```

This data type definition will help developers working on other pages or data sources to prepare the necessary data to be passed to the `ReportScreen`. 