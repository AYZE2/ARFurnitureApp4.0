package com.example.arfurnitureapp.screens


import androidx.compose.material.icons.filled.Camera  // Use Camera instead of CameraAlt
import androidx.compose.material.icons.outlined.CameraAlt  // If you need CameraAlt specifically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.arfurnitureapp.R
import com.example.arfurnitureapp.components.BottomNavigationBar
import com.example.arfurnitureapp.components.SearchBar
import com.example.arfurnitureapp.data.SampleData
import com.example.arfurnitureapp.model.Category
import com.example.arfurnitureapp.model.Product
import com.example.arfurnitureapp.viewmodel.AuthViewModel
import com.example.arfurnitureapp.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController,
               cartViewModel: CartViewModel = viewModel(),  // Add these parameters
               authViewModel: AuthViewModel = viewModel()) {
    val scrollState = rememberScrollState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("" +
                        "ZSS FURNITURE HOUSE") },
                actions = {
                    IconButton(onClick = { /* Handle camera function */ }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                    }
                    IconButton(onClick = {
                        if (currentRoute != "cart") {
                            navController.navigate("cart")
                        } }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController,
                cartViewModel = cartViewModel,  // If you're already passing this
                authViewModel = authViewModel )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            SearchBar(
                onSearch = { query ->
                    // Handle search
                }
            )

            // Featured AR Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)
                    .clickable {
                        navController.navigate("arView/featured")
                    }
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = "AR Experience",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "Try Furniture in Your Space",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    )
                }
            }

            // Categories Section
            SectionHeader(
                title = "Categories",
                onSeeAllClick = { navController.navigate("categories") }
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SampleData.getCategories()) { category ->
                    CategoryItem(
                        category = category,
                        onClick = {
                            navController.navigate("productList/${category.id}")
                        }
                    )
                }
            }

            // Popular Products Section
            SectionHeader(
                title = "Popular Products",
                onSeeAllClick = { /* Handle see all popular products */ }
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SampleData.getPopularProducts()) { product ->
                    ProductItem(
                        product = product,
                        onClick = {
                            navController.navigate("productDetail/${product.id}")
                        }
                    )
                }
            }

            // Recently Viewed
            SectionHeader(
                title = "Recently Viewed",
                onSeeAllClick = { /* Handle see all recently viewed */ }
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SampleData.getRecentlyViewedProducts()) { product ->
                    ProductItem(
                        product = product,
                        onClick = {
                            navController.navigate("productDetail/${product.id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onSeeAllClick) {
            Text("See All")
        }
    }
}

@Composable
fun CategoryItem(category: Category, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick)
    ) {
        Card(
            modifier = Modifier.size(80.dp)
        ) {
            Image(
                painter = painterResource(id = category.imageResId),
                contentDescription = category.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun ProductItem(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Image(
                painter = painterResource(id = product.imageResId),
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "$${product.price}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}