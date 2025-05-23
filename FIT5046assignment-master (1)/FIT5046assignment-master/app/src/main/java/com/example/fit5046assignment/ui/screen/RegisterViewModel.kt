package com.example.fit5046assignment.ui.screen

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val registrationManager = UserRegistrationManager(application)

    var registrationSuccess by mutableStateOf(false)
    var registrationError by mutableStateOf<String?>(null)

    fun register(username: String, email: String, password: String) {
        registrationManager.registerUser(
            email, password, username,
            onSuccess = {
                registrationSuccess = true
                registrationError = null
            },
            onError = { error ->
                registrationError = error
                registrationSuccess = false
            }
        )
    }
}