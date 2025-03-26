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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.arfurnitureapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val scrollState = rememberScrollState()

    // Note: You'll need to add this to your AuthViewModel
    val updateProfileSuccess by remember { mutableStateOf(null) }

    // If not logged in, redirect to login
    if (currentUser == null) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo("editProfile") { inclusive = true }
            }
        }
        return
    }

    var name by remember { mutableStateOf(currentUser?.name ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var address by remember { mutableStateOf(currentUser?.address ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Name input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone input
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Address input
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Success/Failure message
            updateProfileSuccess?.let {
                Text(
                    text = if (it) "Profile updated successfully!" else "Failed to update profile.",
                    color = if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Save button
            Button(
                onClick = {
                    // You'll need to add this method to your AuthViewModel
                    authViewModel.updateUserProfile(name, phone, address)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("SAVE CHANGES")
            }
        }
    }
}