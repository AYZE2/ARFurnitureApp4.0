package com.example.arfurnitureapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.arfurnitureapp.components.BottomNavigationBar
import com.example.arfurnitureapp.viewmodel.AuthViewModel
import com.example.arfurnitureapp.viewmodel.CartViewModel
import com.example.arfurnitureapp.viewmodel.FavoritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    favoritesViewModel: FavoritesViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    val scrollState = rememberScrollState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            // Try to fix any potentially missing user documents
            authViewModel.checkAndFixUserDocument()
        } else {
            // Navigate to login
            navController.navigate("login") {
                popUpTo("profile") { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        if (!isLoggedIn) {
            // Show login prompt if not logged in
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // User profile content
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile header with avatar
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = currentUser?.name?.firstOrNull()?.toString() ?: "U",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User name
                Text(
                    text = currentUser?.name ?: "",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = currentUser?.email ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                // User stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = favoritesViewModel.savedProducts.collectAsState().value.size.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Favorites")
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = cartViewModel.getCartSize().toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Cart Items")
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val orders by remember { mutableStateOf(0) }
                        Text(
                            text = orders.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Orders")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Section header for Account
                SectionHeader(title = "Account")

                // Menu items
                ProfileMenuItem(
                    icon = Icons.Default.Person,
                    title = "Edit Profile",
                    onClick = { navController.navigate("editProfile") }
                )

                ProfileMenuItem(
                    icon = Icons.Default.LocationOn,
                    title = "My Addresses",
                    onClick = { navController.navigate("addresses") }
                )

                ProfileMenuItem(
                    icon = Icons.Default.CreditCard,
                    title = "Payment Methods",
                    onClick = { navController.navigate("paymentMethods") }
                )

                // Section header for Orders
                SectionHeader(title = "Orders & Shopping")

                ProfileMenuItem(
                    icon = Icons.Default.ShoppingBag,
                    title = "My Orders",
                    onClick = { navController.navigate("orders") }
                )

                ProfileMenuItem(
                    icon = Icons.Default.Favorite,
                    title = "My Wishlist",
                    onClick = { navController.navigate("saved") }
                )

                ProfileMenuItem(
                    icon = Icons.Default.ShoppingCart,
                    title = "My Cart",
                    onClick = { navController.navigate("cart") }
                )

                // Section header for Settings
                SectionHeader(title = "Settings")

                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "App Settings",
                    onClick = { /* Navigate to settings */ }
                )

                ProfileMenuItem(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    onClick = { /* Navigate to help */ }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Logout button
                Button(
                    onClick = {
                        authViewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LOG OUT")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Divider()
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}