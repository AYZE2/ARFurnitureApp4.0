package com.example.arfurnitureapp.screens

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.arfurnitureapp.ar.ARSceneView
import com.example.arfurnitureapp.data.SampleData
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ARViewScreen(productId: String, navController: NavController? = null) {
    val context = LocalContext.current
    val product = SampleData.getProductById(productId)
    var scaleFactor by remember { mutableStateOf(1.0f) }
    var rotationAngle by remember { mutableStateOf(0f) }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
            // AR Scene View - This would be your custom AR implementation
            // In a real app, this would use ARCore/SceneView or similar
            ARSceneView(
                productId = productId,
                scaleFactor = scaleFactor,
                rotationAngle = rotationAngle,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Camera permission denied view
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Camera permission is required for AR view")
                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() }
                ) {
                    Text("Grant Permission")
                }
            }
        }

        // AR Controls overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            // Product info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = product?.name ?: "Product",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "$${product?.price}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Button(
                        onClick = { /* Add to cart */ }
                    ) {
                        Text("Add to Cart")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AR Control buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FloatingActionButton(
                    onClick = { /* Take screenshot */ },
                    modifier = Modifier.size(56.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Take Photo",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                FloatingActionButton(
                    onClick = { rotationAngle += 45f },
                    modifier = Modifier.size(56.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Rotate",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                FloatingActionButton(
                    onClick = { scaleFactor += 0.1f },
                    modifier = Modifier.size(56.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(
                        Icons.Default.ZoomIn,
                        contentDescription = "Scale Up",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                FloatingActionButton(
                    onClick = { if (scaleFactor > 0.2f) scaleFactor -= 0.1f },
                    modifier = Modifier.size(56.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(
                        Icons.Default.ZoomOut,
                        contentDescription = "Scale Down",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Top bar with back button
        TopAppBar(
            title = { Text("AR View") },
            navigationIcon = {
                IconButton(onClick = { navController?.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            modifier = Modifier.align(Alignment.TopCenter),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}