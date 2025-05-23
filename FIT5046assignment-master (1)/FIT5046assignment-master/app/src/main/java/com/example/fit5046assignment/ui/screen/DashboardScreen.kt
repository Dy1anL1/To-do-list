package com.example.fit5046assignment.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


//Dashboard Screen
@Composable
fun DashboardScreen( navTo: (String) -> Unit, signOut: () -> Unit) {
    val lightBlue = Color(0xFF90CAF9)
    val context = LocalContext.current
    val userName = remember { mutableStateOf("Hello, User") }
    val userEmail = remember { mutableStateOf("") }

    // Fetch username from Firestore
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("displayName") ?: document.getString("username")
                    val email = document.getString("email")
                    if (!name.isNullOrBlank()) {
                        userName.value = "$name"
                    }

                    if(!email.isNullOrBlank()) {
                        userEmail.value = "$email"
                    }

                }
                .addOnFailureListener {
                    Log.e("DashboardScreen", "Failed to fetch username: ${it.message}")
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // optimize the element's position
        Spacer(modifier = Modifier.height(24.dp))
        // Top Row elements：username
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = userName.value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF1976D2),
                    fontWeight = FontWeight.Bold
                )
            )

            // Logout button
            Button(
                onClick = {
                    // TODO: Implement logout, redirect to the login page
                    signOut()
                    navTo("login")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Logout", color = Color.White)
            }


        }
        // shows the user's email information
        Text(
            text = userEmail.value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.Gray,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Main function list
        val menuItems = listOf(
            Triple(Icons.Rounded.Face, "My day", "my_day"),
            Triple(Icons.Rounded.Star, "Important", "important"),
            Triple(Icons.Rounded.DateRange, "Plan to do", "plan_to_do"),
            Triple(Icons.Rounded.List, "Tasks", "tasks")
        )
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            menuItems.forEach { (icon, text, route) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .border(
                            width = 1.dp,
                            color = lightBlue,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            navTo(route)
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = text,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }


        // Report button
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = "Report",
                tint = Color(0xFF1976D2),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clickable {
                        navTo("report")
                    }
            )
            Text(
                text = "Report",
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    navTo("report")
                }
            )
        }
    }
}