package com.example.arfurnitureapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.arfurnitureapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val loginError by authViewModel.loginError.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    val scrollState = rememberScrollState()

    // If user becomes logged in, navigate to profile
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate("profile") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Logo or Icon
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "ZSS FURNITURE HOUSE",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Error message
                loginError?.let {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }

                // Email input
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        authViewModel.clearLoginError()
                    },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    enabled = !isLoading
                )

                // Password input
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        authViewModel.clearLoginError()
                    },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    enabled = !isLoading
                )

                // Login button
                Button(
                    onClick = {
                        authViewModel.clearLoginError()
                        authViewModel.login(email, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = email.isNotBlank() && password.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("LOGIN")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Forgot password link
                TextButton(
                    onClick = { /* Navigate to forgot password */ },
                    enabled = !isLoading
                ) {
                    Text("Forgot Password?")
                }

                // Sign up link
                TextButton(
                    onClick = { navController.navigate("signup") },
                    enabled = !isLoading
                ) {
                    Text("Don't have an account? Sign up")
                }

                // Demo credentials
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    "Demo credentials:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    "Email: user@example.com\nPassword: password123",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }

            // Full-screen loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}