package com.example.arfurnitureapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun SignupScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordsMatch by remember { mutableStateOf(true) }

    val signupError by authViewModel.signupError.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    val scrollState = rememberScrollState()

    // Check if passwords match when either changes
    LaunchedEffect(password, confirmPassword) {
        passwordsMatch = password == confirmPassword || confirmPassword.isEmpty()
    }

    // If user becomes logged in, navigate to profile
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate("profile") {
                popUpTo("signup") { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign Up") },
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
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Create an Account",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Error message
                signupError?.let {
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

                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        authViewModel.clearSignupError()
                    },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    enabled = !isLoading
                )

                // Email input
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        authViewModel.clearSignupError()
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
                        authViewModel.clearSignupError()
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
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    enabled = !isLoading,
                    supportingText = {
                        Text("Password should be at least 6 characters long")
                    }
                )

                // Confirm Password input
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        authViewModel.clearSignupError()
                    },
                    label = { Text("Confirm Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    enabled = !isLoading,
                    isError = !passwordsMatch,
                    supportingText = {
                        if (!passwordsMatch) {
                            Text("Passwords don't match", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                // Sign up button
                Button(
                    onClick = {
                        if (password != confirmPassword) {
                            // Show error
                            return@Button
                        }
                        authViewModel.clearSignupError()
                        authViewModel.signup(name, email, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = name.isNotBlank() && email.isNotBlank() &&
                            password.isNotBlank() && password == confirmPassword &&
                            password.length >= 6 && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("SIGN UP")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Login link
                TextButton(
                    onClick = { navController.popBackStack() },
                    enabled = !isLoading
                ) {
                    Text("Already have an account? Log in")
                }
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