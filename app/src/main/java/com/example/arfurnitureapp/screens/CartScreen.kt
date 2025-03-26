package com.example.arfurnitureapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.arfurnitureapp.components.BottomNavigationBar
import com.example.arfurnitureapp.viewmodel.CartItem
import com.example.arfurnitureapp.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    cartViewModel: CartViewModel = viewModel()
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val totalPrice by cartViewModel.totalPrice.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping Cart") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        IconButton(onClick = { cartViewModel.clearCart() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Cart")
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            // Empty cart view
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
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your cart is empty",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add items to your cart to see them here",
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
            // Cart with items
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = cartItems,
                        key = { it.product.id }
                    ) { item ->
                        CartItemCard(
                            cartItem = item,
                            onQuantityChange = { quantity ->
                                cartViewModel.updateQuantity(item.product.id, quantity)
                            },
                            onRemove = {
                                cartViewModel.removeFromCart(item.product.id)
                            }
                        )
                    }
                }

                // Checkout section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "$${String.format("%.2f", totalPrice)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { navController.navigate("checkout") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Proceed to Checkout")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    cartItem: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image
            Image(
                painter = painterResource(id = cartItem.product.imageResId),
                contentDescription = cartItem.product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
            )

            // Product details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = cartItem.product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$${cartItem.product.price}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quantity controls
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onQuantityChange(cartItem.quantity - 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Decrease"
                        )
                    }

                    Text(
                        text = cartItem.quantity.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = { onQuantityChange(cartItem.quantity + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Increase"
                        )
                    }
                }
            }

            // Remove button
            IconButton(
                onClick = onRemove
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}