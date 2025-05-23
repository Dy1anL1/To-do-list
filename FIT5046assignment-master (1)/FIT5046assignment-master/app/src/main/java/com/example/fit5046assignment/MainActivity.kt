package com.example.fit5046assignment

//import screens from ui/screens
import android.os.Build
import com.example.fit5046assignment.ui.screen.LoginScreen
import com.example.fit5046assignment.ui.screen.RegisterScreen
import com.example.fit5046assignment.ui.screen.DashboardScreen
import com.example.fit5046assignment.ui.screen.MyDayScreen
import com.example.fit5046assignment.ui.screen.ImportantScreen
import com.example.fit5046assignment.ui.screen.PlanToDoScreen
import com.example.fit5046assignment.ui.screen.TaskScreen

//import important libs
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fit5046assignment.roomDb.TaskEntity
import com.example.fit5046assignment.ui.screen.AddPlanTaskDialog
import com.example.fit5046assignment.ui.screen.AddTaskDialog
import com.example.fit5046assignment.ui.screen.AuthManager
import com.example.fit5046assignment.ui.screen.ResetPasswordScreen
import com.example.fit5046assignment.ui.screen.Report
import com.example.fit5046assignment.ui.screen.TaskViewModel


class MainActivity : ComponentActivity() {
    private lateinit var authManager: AuthManager
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private var pendingNavigation: (() -> Unit)? = null
    private var onLoginError: ((String) -> Unit)? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        authManager = AuthManager(this)
        
        // Set up Google Sign-In launcher
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            authManager.handleSignInResult(result.data, 
                onSuccess = { user ->
                    Log.d("MainActivity", "Google sign-in successful: ${user.email}")
                    pendingNavigation?.invoke()
                },
                onError = { errorMsg ->
                    Log.e("MainActivity", "Google sign-in failed: $errorMsg")
                    onLoginError?.invoke(errorMsg)
                }
            )
        }
        
        setContent {
            MyApp()
        }
    }
    
    fun startGoogleSignIn(onSuccess: () -> Unit, onError: (String) -> Unit) {
        pendingNavigation = onSuccess
        onLoginError = onError
        googleSignInLauncher.launch(authManager.getSignInIntent())
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var loginError by rememberSaveable { mutableStateOf(false) }

    val viewModel: TaskViewModel = viewModel()

    val tasks by viewModel.allTasks.collectAsState(initial = emptyList())

    var showImportantAddDialog by remember { mutableStateOf(false) }

    var showPlanToDoAddDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as MainActivity


    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLogin = { username, password ->
                    if (username == "admin" && password == "1234") {
                        loginError = false
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        loginError = true
                    }
                },
                onResetPasswordClick = {
                    navController.navigate("reset_password")
                },
                onRegisterClick = {
                    navController.navigate("register")
                },
                loginError = loginError,
                onGoogleLogin = {
                    Toast.makeText(context, "Starting Google login...", Toast.LENGTH_SHORT).show()
                    activity.startGoogleSignIn(
                        onSuccess = {
                            loginError = false
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onError = { errorMsg ->
                            loginError = true
                            Toast.makeText(context, "Google Sign-in failed: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                navController = navController
            )
        }

        composable("home") {
            DashboardScreen(navTo = { route -> navController.navigate(route) }, signOut = {})
        }

        composable("my_day") {
            MyDayScreen(navController)
        }
        composable("important") {
            ImportantScreen(
                navController = navController
            )
        }
        composable("plan_to_do") {
            PlanToDoScreen(
                navController = navController,
                tasks = tasks,
                onTaskCompleteChange = { task, isCompleted ->
                    viewModel.updateTaskCompletionStatus(task, isCompleted)
                },
                onTaskDeleted = { task ->
                    viewModel.deleteTask(task)
                },
                onTaskAdded = { task ->
                    viewModel.insertTask(task)
                }
            )
        }
        composable("tasks") {
            TaskScreen(
                navController = navController,
                tasks = tasks,
                onDeleteTask = { task ->
                    viewModel.deleteTask(task)
                },
                onTaskUpdated = { updatedTask ->
                    viewModel.updateTask(updatedTask)
                }
            )
        }

        composable("report") {
            Report(onNavigateHome = { navController.popBackStack() })
        }



        composable("reset_password") {
            ResetPasswordScreen(
                onReset = { email ->
                    // TODO: add reset password logic, e.g. toast notice
                    println("Sending reset link to: $email")
                },
                onBackToLogin = {
                    navController.navigate("login") {
                        popUpTo("reset_password") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegister = { username, email, password ->
                    // TODO: Can add registration logic, such as writing to the database, validation, etc
                    println("Registering user: $username, $email")

                    // After successful registration, back to the login page
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                // if user have an account user can go back to login page
                onBackToLogin = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResetPasswordScreenPreview() {
    ResetPasswordScreen(
        onReset = { /* preview only */ },
        onBackToLogin = { /* preview only */ }
    )
}

