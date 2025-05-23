package com.example.fit5046assignment.ui.screen

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseUser

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val loginHelper = LoginManager(application.applicationContext)

    var loginSuccess = mutableStateOf(false)
    var loginError = mutableStateOf<String?>(null)
    var currentUser = mutableStateOf<FirebaseUser?>(null)

    fun login(email: String, password: String) {
        loginHelper.loginUser(
            email, password,
            onSuccess = { user ->
                currentUser.value = user
                loginSuccess.value = true
                loginError.value = null
            },
            onError = { error ->
                loginError.value = error
                loginSuccess.value = false
            }
        )
    }

    fun logout() {
        loginHelper.signOut()
        currentUser.value = null
        loginSuccess.value = false
    }
}
