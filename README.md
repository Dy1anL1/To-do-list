# TaskPro - Task Management Android Application

## Overview
TaskPro is a comprehensive task management Android application built with Jetpack Compose, Room database, and Firebase integration. The app helps users organise their daily tasks with features like importance marking, calendar synchronisation, task reporting, and Google Sign-In authentication.

## Features

### 📋 Core Task Management
- **Create Tasks**: Add new tasks with custom names and due dates
- **Task Categories**: Organise tasks into different views:
  - **My Day**: Daily task overview and manage tasks
  - **Important**: Tasks marked as important with star priority
  - **Plan to Do**: All planned tasks with completion tracking
  - **Tasks**: All tasks list with editing capabilities
- **Task Status**: Track completion, overdue status, and importance
- **Task Modification**: Edit task details, due dates, and importance levels

### 🔐 Authentication System
- **Local Authentication**: Username/password login
- **Google Sign-In**: OAuth integration with Firebase authentication
- **User Registration**: Account creation with email validation
- **Password Reset**: Reset password functionality
- **User Profile**: Display user information from Firebase Firestore

### 📅 Calendar Integration
- **System Calendar Sync**: Automatically sync tasks to device Google calendar
- **All-Day Events**: Tasks appear as all-day calendar events
- **Important Task Marking**: Important tasks marked with ⭐ prefix
- **Timezone Handling**: UTC timezone consistency across devices
- **Calendar Permissions**: Read/write calendar access management

### 📊 Advanced Reporting
- **Task Status Summary**: Overview of upcoming, overdue, and completed tasks
- **Completion Percentage**: Visual progress tracking
- **Date Filtering**: Filter reports by:
  - All time
  - Last 1 week
  - Last 2 weeks
- **Visual Charts**: Data visualisation using Vico charts library

### 🎨 Modern UI/UX
- **Material Design 3**: Modern Material You design system
- **Jetpack Compose**: Declarative UI framework
- **Responsive Layout**: Adaptive design for different screen sizes
- **Custom Theming**: Branded colour scheme and typography
- **Intuitive Navigation**: Bottom navigation and proper navigation flow

## Technical Architecture

### 🏗️ Architecture Pattern
- **MVVM (Model-View-ViewModel)**: Clean architecture separation
- **Repository Pattern**: Data layer abstraction
- **Single Activity**: Navigation managed through Jetpack Navigation Compose

### 🗄️ Data Layer
- **Room Database**: Local SQLite database with:
  - `TaskEntity`: Core task data model
  - `TaskDao`: Database access operations
  - `TaskRepository`: Data repository abstraction
- **Firebase Firestore**: Cloud storage for user profiles
- **Firebase Authentication**: User authentication management

### 📱 UI Layer
- **Jetpack Compose**: 100% declarative UI
- **Navigation Compose**: Type-safe navigation
- **Material 3**: Modern design components
- **State Management**: Reactive UI with StateFlow and Compose State

### 🔧 Key Components

#### Data Models
```kotlin
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val creationDate: String,
    val dueDate: String,
    val isImportant: Boolean = false,
    val isCompleted: Boolean = false,
    val isOverdue: Boolean = false
)
```

#### Main Screens
- `LoginScreen`: Authentication interface
- `DashboardScreen`: Main navigation hub
- `MyDayScreen`: Daily task overview
- `ImportantScreen`: Important tasks management
- `PlanToDoScreen`: Task planning interface
- `TaskScreen`: Comprehensive task management
- `ReportScreen`: Analytics and reporting

## Dependencies

### Core Android
- **Kotlin**: Programming language
- **Compose BOM**: UI framework version management
- **Navigation Compose**: Screen navigation
- **Room**: Local database
- **Material 3**: UI components

### Firebase & Authentication
- **Firebase Auth**: User authentication
- **Firebase Firestore**: Cloud database
- **Google Play Services Auth**: Google Sign-In
- **Credentials API**: Modern authentication flow

### Data Visualisation
- **Vico Charts**: Chart library for reporting

### Calendar Integration
- **Calendar Provider**: System calendar access
- **Google Calendar API**: Advanced calendar features

## Installation & Setup

### Prerequisites
1. **Android Studio**: Latest stable version (Hedgehog or newer)
2. **JDK 11+**: Java Development Kit
3. **Android SDK**: API level 24+ (Android 7.0)
4. **Firebase Project**: Google Firebase account

### Firebase Configuration
1. Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Enable **Authentication** and **Firestore Database**
3. Add your Android app to the Firebase project
4. Download `google-services.json` and place it in `app/` directory
5. Configure Google Sign-In:
   - Add SHA-1 fingerprint to Firebase project
   - Note the Web client ID for OAuth configuration

### Project Setup
1. **Clone the repository**:
   ```bash
   git clone https://github.com/Airius2001/FIT5046assignment.git
   cd FIT5046assignment
   ```

2. **Configure Firebase**:
   - Place `google-services.json` in `app/` directory
   - Update `FirebaseConfig.kt` with your project details

3. **Update Web Client ID**:
   - Add your OAuth 2.0 Web client ID to `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="default_web_client_id">YOUR_WEB_CLIENT_ID</string>
   ```

4. **Sync and Build**:
   - Open project in Android Studio
   - Sync Gradle files
   - Build and run on device/emulator

### Permissions Setup
The app requires the following permissions:
- `INTERNET`: Firebase and network operations
- `ACCESS_NETWORK_STATE`: Network connectivity checks
- `READ_CALENDAR`: Calendar synchronisation
- `WRITE_CALENDAR`: Calendar event creation

## Usage Guide

### Authentication
1. **Local Login**: Use credentials `admin`/`1234` for testing
2. **Google Sign-In**: Tap Google button for OAuth authentication
3. **Registration**: Create new account with email validation

### Task Management
1. **Create Task**: Use + button in any task screen
2. **Mark Important**: Tap star icon to prioritise tasks
3. **Complete Task**: Check checkbox to mark as done
4. **Edit Task**: Long press or use edit options
5. **Delete Task**: Swipe or use delete button

### Calendar Sync
- Tasks automatically sync to system calendar
- Important tasks display with ⭐ prefix
- Grant calendar permissions when prompted

### Reporting
- Navigate to Reports from dashboard
- View task statistics and completion rates
- Filter by time periods for detailed analysis

## Development Features

### Database Schema
- **Version 3**: Current database schema
- **Auto-migration**: Handles schema updates
- **Sample Data**: Pre-populated demo tasks

### Error Handling
- **Network Error Handling**: Graceful offline support
- **Authentication Errors**: Detailed error messages
- **Database Exceptions**: Robust error recovery

### Logging
- **Firebase Auth Logs**: Authentication debugging
- **Calendar Sync Logs**: Calendar operation tracking
- **Task Operation Logs**: Database operation monitoring

## Code Structure

```
app/src/main/java/com/example/fit5046assignment/
├── MainActivity.kt                 # Main activity and navigation
├── FirebaseConfig.kt              # Firebase configuration
├── ui/
│   ├── screen/                    # All screen composables
│   │   ├── AuthManager.kt         # Authentication logic
│   │   ├── TaskViewModel.kt       # Task business logic
│   │   ├── LoginScreen.kt         # Login interface
│   │   ├── DashboardScreen.kt     # Main dashboard
│   │   ├── MyDayScreen.kt         # Daily tasks view
│   │   ├── ImportantScreen.kt     # Important tasks
│   │   ├── PlanToDoScreen.kt      # Task planning
│   │   ├── TaskScreen.kt          # Task management
│   │   ├── ReportScreen.kt        # Analytics
│   │   └── RegisterScreen.kt      # User registration
│   └── theme/                     # App theming
│       ├── Color.kt               # Color definitions
│       ├── Theme.kt               # Material theme
│       └── Type.kt                # Typography
├── viewmodel/
│   └── ReportViewModel.kt         # Reporting logic
└── roomDb/                        # Database layer
    ├── AppDatabase.kt             # Database configuration
    ├── TaskEntity.kt              # Task data model
    ├── TaskDao.kt                 # Data access object
    ├── TaskRepository.kt          # Repository pattern
    └── TaskModificationDao.kt     # Modification operations
```

## Contributing

### Development Guidelines
1. **Coding Standards**: Follow Kotlin conventions
2. **Comments**: Use Australian English (EN-AU)
3. **Architecture**: Maintain MVVM pattern
4. **Testing**: Add unit tests for new features
5. **Documentation**: Update README for major changes

### Future Enhancements
- [ ] Task categories and tags
- [ ] Recurring task support
- [ ] Dark mode theme
- [ ] Widget support
- [ ] Backup and restore
- [ ] Task sharing
- [ ] Notification reminders
- [ ] Advanced search and filtering

## Troubleshooting

### Common Issues

**Google Sign-In Fails**:
- Verify SHA-1 fingerprint in Firebase Console
- Check Web client ID configuration
- Ensure internet connectivity

**Calendar Sync Not Working**:
- Grant calendar permissions in device settings
- Check date format compatibility
- Verify calendar provider availability

**App Crashes on Startup**:
- Ensure `google-services.json` is properly configured
- Check Firebase project settings
- Verify all dependencies are compatible

### Debug Logs
Enable debug logging by setting log level in `AuthManager.kt` and `TaskViewModel.kt`.

## License

This project is developed for educational purposes as part of FIT5046 assignment. All rights reserved.

## Support

For technical issues or questions:
1. Check troubleshooting guide above
2. Review Firebase configuration
3. Verify all dependencies are up to date
4. Check Android Studio and SDK versions

---

**Built with ❤️ using Modern Android Development practices by Weiliang Huang, Jiaze Li, Jiahang Sun, Zexin Xu** 