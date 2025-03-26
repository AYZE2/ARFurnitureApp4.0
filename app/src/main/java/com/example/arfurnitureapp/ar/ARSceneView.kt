package com.example.arfurnitureapp.ar

import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.arfurnitureapp.R
import com.example.arfurnitureapp.data.SampleData
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode

private const val TAG = "ARSceneView"

@Composable
fun ARSceneView(
    productId: String,
    scaleFactor: Float,
    rotationAngle: Float,
    modifier: Modifier = Modifier
) {
    // Get the context
    val ctx = LocalContext.current
    val product = SampleData.getProductById(productId)

    // Create the AR view
    AndroidView(
        modifier = modifier,
        factory = { context ->
            // For development purposes, use a placeholder view
            // This lets you test navigation without AR implementation
            val frameLayout = FrameLayout(context)

            try {
                // Create the AR scene view
                val arSceneView = ArSceneView(context)
                frameLayout.addView(arSceneView)

                // Set up AR session created callback
                arSceneView.onArSessionCreated = {
                    try {
                        // Get model resource ID
                        val modelResourceId = product?.modelResId ?: R.raw.model_lamp

                        // Create model node using the engine from arSceneView
                        val modelNode = ArModelNode(
                            engine = arSceneView.engine,
                            placementMode = PlacementMode.INSTANT
                        )

                        // Load the model
                        // Converting the resource ID to a file path string
                        val modelPath = "models/${context.resources.getResourceEntryName(modelResourceId)}.glb"

                        modelNode.loadModelGlbAsync(
                            glbFileLocation = modelPath,
                            autoAnimate = true,
                            scaleToUnits = scaleFactor,
                            onError = { error ->
                                Log.e(TAG, "Error loading model", error)
                            },
                            onLoaded = { _ ->
                                Log.d(TAG, "Model loaded successfully")
                                // You can update rotation here if needed
                            }
                        )

                        // Add model to scene
                        arSceneView.addChild(modelNode)

                    } catch (e: Exception) {
                        Log.e(TAG, "Error setting up AR model", e)
                    }
                }
            } catch (e: Exception) {
                // If AR setup fails, show a placeholder
                Log.e(TAG, "Error initializing AR view", e)
                val fallbackView = View(context)
                fallbackView.setBackgroundColor(android.graphics.Color.DKGRAY)
                frameLayout.addView(fallbackView)
            }

            frameLayout
        }
    )
}