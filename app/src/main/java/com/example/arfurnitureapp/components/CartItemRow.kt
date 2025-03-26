package com.example.arfurnitureapp.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.arfurnitureapp.viewmodel.CartItem

@Composable
fun CartItemRow(
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