package com.example.fit5046assignment.ui.screen

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.fit5046assignment.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore


class AuthManager(private val context: Context) {
    private val auth: FirebaseAuth = Firebase.auth
    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun handleSignInResult(data: Intent?, onSuccess: (FirebaseUser) -> Unit, onError: (String) -> Unit) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            
            Log.d("AuthManager", "Google sign-in successful: ${account.email}")
            
            val idToken = account.idToken
            if (idToken != null) {
                firebaseAuthWithGoogle(idToken, onSuccess, onError)
            } else {
                onError("Failed to get ID token from Google account")
            }
        } catch (e: ApiException) {
            Log.e("AuthManager", "Google sign-in ApiException - Status Code: ${e.statusCode}", e)
            Log.e("AuthManager", "Google sign-in ApiException - Status Message: ${e.status}", e)
            Log.e("AuthManager", "Google sign-in ApiException - Resolution: ${e.status.resolution}", e)
            
            val errorMessage = when (e.statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Google sign-in was cancelled"
                GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Google sign-in failed - please try again"
                GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS -> "Google sign-in is already in progress"
                GoogleSignInStatusCodes.INVALID_ACCOUNT -> "Invalid Google account"
                GoogleSignInStatusCodes.NETWORK_ERROR -> {
                    Log.e("AuthManager", "Network error details: Check internet connection and Firebase configuration")
                    "Network error: Please check your internet connection and try again. If the problem persists, the SHA-1 fingerprint may not be registered in Firebase Console."
                }
                GoogleSignInStatusCodes.INTERNAL_ERROR -> "Internal error during Google sign-in - please try again"
                else -> "Google sign-in error: ${e.statusCode} - ${e.localizedMessage}"
            }
            Log.e("AuthManager", "Google sign-in failed: $errorMessage", e)
            onError(errorMessage)
        } catch (e: Exception) {
            Log.e("AuthManager", "Unexpected error during Google sign-in", e)
            onError("Unexpected error: ${e.localizedMessage}")
        }
    }

    private fun firebaseAuthWithGoogle(
        idToken: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val db = FirebaseFirestore.getInstance()
                        val userData = hashMapOf(
                            "uid" to user.uid,
                            "email" to user.email,
                            "username" to user.email,
                            "createdAt" to System.currentTimeMillis()
                        )

                        db.collection("users").document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Log.d("AuthManager", "Google user profile saved to Firestore")
                                onSuccess(user)
                            }
                            .addOnFailureListener { e ->
                                Log.e("AuthManager", "Failed to save Google user profile: ${e.message}")
                                onError("Google login successful, but failed to store user profile")
                            }
                    } else {
                        onError("User is null")
                    }
                } else {
                    onError(task.exception?.localizedMessage ?: "Authentication failed")
                }
            }
    }

    fun signOut(onFinished: () -> Unit = {}) {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            Log.d("AuthManager", "Google sign-out completed")
            onFinished()
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser
}

