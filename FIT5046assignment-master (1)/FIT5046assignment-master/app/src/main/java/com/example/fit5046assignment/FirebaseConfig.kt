package com.example.fit5046assignment

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseConfig {
    fun initializeFirebase(context: Context) {
        val options = FirebaseOptions.Builder()
            .setApplicationId("1:YOUR_PROJECT_NUMBER:android:YOUR_APP_ID") // Your Firebase app ID
            .setApiKey("YOUR_API_KEY") // Your API key
            .setProjectId("your-project-id") // Your project ID
            .build()

        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context, options)
        }
    }
} 