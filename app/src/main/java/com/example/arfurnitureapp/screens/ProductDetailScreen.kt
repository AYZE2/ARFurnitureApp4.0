package com.example.arfurnitureapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.arfurnitureapp.components.BottomNavigationBar
import com.example.arfurnitureapp.data.SampleData
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arfurnitureapp.viewmodel.CartViewModel
import com.example.arfurnitureapp.viewmodel.FavoritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(productId: String,
                        navController: NavController,
                        favoritesViewModel: FavoritesViewModel = viewModel(),
                        cartViewModel: CartViewModel = viewModel()
) {
    val product = SampleData.getProductById(productId)
    val savedProductIds by favoritesViewModel.savedProductIds.collectAsState()
    val isFavorite by remember(savedProductIds) {
        derivedStateOf {
            product?.id?.let { savedProductIds.contains(it) } ?: false
        }
    }

    val isLoading by favoritesViewModel.isLoading.collectAsState()
    // Show error if needed
    val error by favoritesViewModel.error.collectAsState()

    // Show error in a snackbar if present
    error?.let {
        LaunchedEffect(it) {
            // Show error message
            // Then clear the error
            favoritesViewModel.clearError()
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product?.name ?: "Product Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (product != null) {
                            favoritesViewModel.toggleFavorite(product)
                        }
                    }, enabled = !isLoading) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = { /* Handle share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            if (product != null) {
                // Product Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    Image(
                        painter = painterResource(id = product.imageResId),
                        contentDescription = product.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    // AR View button overlay
                    Button(
                        onClick = { navController.navigate("arView/$productId") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.ViewInAr,
                            contentDescription = "View in AR",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View in Your Room")
                    }
                }

                // Product Information
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = product.category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$${product.price}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Product Details and Specifications
                    Text(
                        text = "Product Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Sample product details - would come from product data in a real app
                    ProductDetailItem("Materials", "Particle board, Paper foil, Plastic edging")
                    ProductDetailItem("Dimensions", "Width: 70 cm, Depth: 80 cm, Height: 75 cm")
                    ProductDetailItem("Article Number", "123.456.78")

                    Spacer(modifier = Modifier.height(32.dp))

                    // Call to action buttons
                    Button(
                        onClick = {
                            if (product != null) {
                                cartViewModel.addToCart(product)
                                // Optionally add a confirmation message
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Cart")
                    }


                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { navController.navigate("arView/$productId") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.ViewInAr,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View in AR")
                    }
                }
            } else {
                // Product not found
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Product not found")
                }
            }
        }
    }
}

@Composable
fun ProductDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}