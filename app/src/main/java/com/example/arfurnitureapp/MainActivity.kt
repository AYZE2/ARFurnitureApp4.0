package com.example.arfurnitureapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.arfurnitureapp.di.DependencyContainer
import com.example.arfurnitureapp.screens.ARViewScreen
import com.example.arfurnitureapp.screens.AddressManagementScreen
import com.example.arfurnitureapp.screens.CartScreen
import com.example.arfurnitureapp.screens.CatergoriesScreen
import com.example.arfurnitureapp.screens.CheckoutScreen
import com.example.arfurnitureapp.screens.EditProfileScreen
import com.example.arfurnitureapp.screens.HomeScreen
import com.example.arfurnitureapp.screens.LoginScreen
import com.example.arfurnitureapp.screens.OrderDetailScreen
import com.example.arfurnitureapp.screens.OrderHistoryScreen
import com.example.arfurnitureapp.screens.PaymentMethodManagementScreen
import com.example.arfurnitureapp.screens.ProductDetailScreen
import com.example.arfurnitureapp.screens.ProductListScreen
import com.example.arfurnitureapp.screens.ProfileScreen
import com.example.arfurnitureapp.screens.SavedItemsScreen
import com.example.arfurnitureapp.screens.SearchScreen
import com.example.arfurnitureapp.screens.SignupScreen
import com.example.arfurnitureapp.ui.theme.ARFurnitureAppTheme
import com.example.arfurnitureapp.viewmodel.AddressViewModel
import com.example.arfurnitureapp.viewmodel.AuthViewModel
import com.example.arfurnitureapp.viewmodel.CartViewModel
import com.example.arfurnitureapp.viewmodel.FavoritesViewModel
import com.example.arfurnitureapp.viewmodel.OrderViewModel
import com.example.arfurnitureapp.viewmodel.PaymentMethodViewModel
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase is now initialized in the Application class
        // No need to initialize it here

        setContent {
            ARFurnitureAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    IKEARCloneApp()
                }
            }
        }
    }
}

@Composable
fun IKEARCloneApp() {
    val navController = rememberNavController()
    val favoritesViewModel: FavoritesViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val addressViewModel: AddressViewModel = viewModel()
    val paymentMethodViewModel: PaymentMethodViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("categories") {
            CatergoriesScreen(navController = navController)
        }
        composable("productList/{categoryId}") { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")
            ProductListScreen(categoryId = categoryId ?: "", navController = navController)
        }
        composable("productDetail/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductDetailScreen(
                productId = productId ?: "",
                navController = navController,
                favoritesViewModel = favoritesViewModel,
                cartViewModel = cartViewModel
            )
        }
        composable("arView/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ARViewScreen(productId = productId ?: "")
        }
        composable("saved") {
            SavedItemsScreen(
                navController = navController,
                favoritesViewModel = favoritesViewModel
            )
        }
        composable("search") {
            SearchScreen(navController = navController)
        }
        composable("cart") {
            CartScreen(
                navController = navController,
                cartViewModel = cartViewModel
            )
        }
        composable("login") {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable("signup") {
            SignupScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable("profile") {
            ProfileScreen(
                navController = navController,
                authViewModel = authViewModel,
                favoritesViewModel = favoritesViewModel,
                cartViewModel = cartViewModel
            )
        }
        composable("editProfile") {
            EditProfileScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable("checkout") {
            CheckoutScreen(
                navController = navController,
                cartViewModel = cartViewModel,
                checkoutViewModel = viewModel(),
                authViewModel = authViewModel
            )
        }

        // New screens for address, payment, and order management
        composable("addresses") {
            AddressManagementScreen(
                navController = navController,
                addressViewModel = addressViewModel
            )
        }

        composable("paymentMethods") {
            PaymentMethodManagementScreen(
                navController = navController,
                paymentMethodViewModel = paymentMethodViewModel,
                addressViewModel = addressViewModel
            )
        }

        composable("orders") {
            OrderHistoryScreen(
                navController = navController,
                orderViewModel = orderViewModel
            )
        }

        composable("orderDetail/{orderId}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailScreen(
                orderId = orderId,
                navController = navController,
                orderViewModel = orderViewModel
            )
        }
    }
}
