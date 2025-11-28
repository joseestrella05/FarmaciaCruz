package edu.ucne.farmaciacruz.presentation.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.ucne.farmaciacruz.domain.model.Order

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisOrdenesScreen(
    viewModel: MisOrdenesViewModel = hiltViewModel(),
    onOrderClick: (orderId: Int) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Órdenes") }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.error != null -> {
                    ErrorSection(
                        message = state.error ?: "Error desconocido",
                        onRetry = { viewModel.loadOrders() }
                    )
                }

                state.ordenes.isEmpty() -> {
                    EmptyOrdersSection()
                }

                else -> {
                    OrdersList(
                        orders = state.ordenes,
                        onOrderClick = onOrderClick
                    )
                }
            }
        }
    }
}

@Composable
private fun OrdersList(
    orders: List<Order>,
    onOrderClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(orders) { order ->
            OrderCard(order, onOrderClick)
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onClick: (Int) -> Unit
) {
    ElevatedCard(
        onClick = { onClick(order.orderId) },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Orden #${order.orderId}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Estado: ${order.estado}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Total: RD$ ${order.total}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Fecha: ${order.fechaCreacion}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun EmptyOrdersSection() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ReceiptLong,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(70.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "No tienes órdenes aún",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "¡Las órdenes que realices aparecerán aquí!",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
private fun ErrorSection(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(60.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}
