package com.example.arfurnitureapp.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.arfurnitureapp.viewmodel.AuthViewModel
import com.example.arfurnitureapp.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(
    navController: NavController,
    cartViewModel: CartViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val cartSize = cartViewModel.getCartSize()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == "home",
            onClick = {
                if (currentRoute != "home") {
                    navController.navigate("home")
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Category, contentDescription = "Categories") },
            label = { Text("Categories") },
            selected = currentRoute == "categories",
            onClick = {
                if (currentRoute != "categories") {
                    navController.navigate("categories")
                }
            }
        )

        NavigationBarItem(
            icon = {
                BadgedBox(
                    badge = {
                        if (cartSize > 0) {
                            Badge { Text(cartSize.toString()) }
                        }
                    }
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                }
            },
            label = { Text("Cart") },
            selected = currentRoute == "cart",
            onClick = {
                if (currentRoute != "cart") {
                    navController.navigate("cart")
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Saved") },
            label = { Text("Saved") },
            selected = currentRoute == "saved",
            onClick = {
                if (currentRoute != "saved") {
                    navController.navigate("saved")
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentRoute == "profile" || currentRoute == "login" || currentRoute == "signup" || currentRoute == "editProfile",
            onClick = {
                if (isLoggedIn) {
                    // If logged in, go to profile
                    if (currentRoute != "profile") {
                        navController.navigate("profile") {
                            popUpTo("home")
                            launchSingleTop = true
                        }
                    }
                } else {
                    // If not logged in, go to login
                    if (currentRoute != "login") {
                        navController.navigate("login") {
                            popUpTo("home")
                            launchSingleTop = true
                        }
                    }

                }
            }
        )
    }
}