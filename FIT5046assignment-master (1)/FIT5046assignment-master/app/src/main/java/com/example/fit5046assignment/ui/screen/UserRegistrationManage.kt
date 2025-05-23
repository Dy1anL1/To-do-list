package com.example.fit5046assignment.ui.screen

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class UserRegistrationManager(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun registerUser(
        email: String,
        password: String,
        username: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        // Input validation
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onError("Please enter a valid email address.")
            return
        }

        if (password.isBlank() || password.length < 6) {
            onError("Password must be at least 6 characters.")
            return
        }

        if (username.isBlank()) {
            onError("Username cannot be empty.")
            return
        }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val userData = hashMapOf(
                            "uid" to user.uid,
                            "email" to user.email,
                            "username" to username,
                            "createdAt" to System.currentTimeMillis()
                        )

                        firestore.collection("users")
                            .document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                onSuccess(user)
                            }
                            .addOnFailureListener { e ->
                                onError("Failed to store user profile: ${e.message}")
                            }
                    } else {
                        onError("User is null after registration")
                    }
                } else {
                    onError(task.exception?.message ?: "Registration failed")
                }
            }
    }
}
