package com.example.fit5046assignment.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.fit5046assignment.R

//import visibility icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController


// Login Screen
@Composable
fun LoginScreen(onLogin: (String, String) -> Unit,
                onResetPasswordClick: () -> Unit,
                onRegisterClick: () -> Unit,
                loginError: Boolean,
                onGoogleLogin: () -> Unit,
                viewModel: LoginViewModel = viewModel(),
                navController: NavController
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val errorMessage = viewModel.loginError.value
    val context = LocalContext.current


    Surface(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxSize(),
            // make the logo picture up a little bit
            verticalArrangement = Arrangement.Top
        ) {
            //logo's location fix a bit
            Spacer(modifier = Modifier.height(60.dp))

            // import the logo image
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape) //change the logo pic into a circle shape
                    .border(2.dp, Color.Gray, CircleShape) // beautify the logo pic
                    .shadow(4.dp, CircleShape) // beautify the logo pic (added shadow)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Hello Again!",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally))

            Spacer(modifier = Modifier.height(8.dp)) // create a space between these two components

            Text(
                text = "Welcome back, You've been missed!",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //add password visibility to increase usability
            var passwordVisible by remember { mutableStateOf(false) }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else
                        Icons.Filled.VisibilityOff

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = image,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Gray)) {
                        append("Forgot password? ")
                    }
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                        append("Reset password")
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable {
                        onResetPasswordClick()
                    }
            )


            Spacer(modifier = Modifier.height(24.dp))

            Button(
//                onClick = { onLogin(username, password) },
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }

            LaunchedEffect(errorMessage) {
                errorMessage?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    // Clear the error after showing
                    viewModel.loginError.value = null
                }
            }

            LaunchedEffect(viewModel.loginSuccess.value) {
                if (viewModel.loginSuccess.value) {
                    // Navigate to home screen
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                    Toast.makeText(context, "Log in successfully!", Toast.LENGTH_LONG).show()
                }
            }

//            LaunchedEffect(viewModel.loginError.value) {
//                viewModel.loginError.value?.let {
//                    Toast.makeText(context, "Log in details incorrect", Toast.LENGTH_LONG).show()
//                }
//            }

            // add login error message
            if (loginError) {
            Text(
                text = "Wrong username or password, please re-enter",
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            )
        }

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Or",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(5.dp))

            //google login
            OutlinedButton(
                onClick = { onGoogleLogin() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "Google Icon",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Login with Google")
            }

            Text(
                text = "Don't have an account? Register",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable {
                        // click here to jump to the register page (haven't complete register page yet)
                        onRegisterClick()
                    }
                    .padding(top = 8.dp)
            )
        }
    }
}