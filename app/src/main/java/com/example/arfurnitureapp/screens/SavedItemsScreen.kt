package com.example.arfurnitureapp.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.arfurnitureapp.components.BottomNavigationBar
import com.example.arfurnitureapp.components.ProductHorizontalItem
import com.example.arfurnitureapp.viewmodel.FavoritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedItemsScreen(
    navController: NavController,
    favoritesViewModel: FavoritesViewModel
) {
    // Collect the saved products state
    val savedProducts by favoritesViewModel.savedProducts.collectAsState()
    val isLoading by favoritesViewModel.isLoading.collectAsState()
    val error by favoritesViewModel.error.collectAsState()

    // Show error if present
    error?.let {
        LaunchedEffect(it) {
            // Show error snackbar
            favoritesViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Items") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { favoritesViewModel.refreshFavorites() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        if (isLoading && savedProducts.isEmpty()) {
            // Show loading indicator
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (savedProducts.isEmpty()) {
            // Show empty state
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "No saved items",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Items you save will appear here",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate("home") }
                    ) {
                        Text("Browse Products")
                    }
                }
            }
        } else {
            // Show favorites list
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = savedProducts,
                    key = { it.id }
                ) { product ->
                    ProductHorizontalItem(
                        product = product,
                        onClick = {
                            navController.navigate("productDetail/${product.id}")
                        },
                        isFavorite = true,
                        onFavoriteToggle = {
                            favoritesViewModel.toggleFavorite(product)
                        },
                        onRemove = {
                            favoritesViewModel.removeFromFavorites(product.id)
                        }
                    )
                }
            }
        }
    }
}