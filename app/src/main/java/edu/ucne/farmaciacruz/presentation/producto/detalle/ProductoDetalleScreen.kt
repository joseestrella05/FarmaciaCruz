package edu.ucne.farmaciacruz.presentation.producto.detalle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoDetalleScreen(
    onBack: () -> Unit,
    viewModel: ProductoDetalleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ProductoDetalleUiEvent.ShowError ->
                    snackbarHostState.showSnackbar(event.message)

                is ProductoDetalleUiEvent.ShowSuccess ->
                    snackbarHostState.showSnackbar(event.message)

                is ProductoDetalleUiEvent.NavigateBack ->
                    onBack()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Producto") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(ProductoDetalleEvent.NavigateBack) }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                ErrorWithRetry(
                    message = state.error ?: "Error desconocido",
                    onRetry = { viewModel.onEvent(ProductoDetalleEvent.NavigateBack) }
                )
            }

            state.producto != null -> {
                ProductoDetalleContent(
                    modifier = Modifier.padding(paddingValues),
                    state = state,
                    onEvent = viewModel::onEvent
                )
            }
        }
    }
}

@Composable
private fun ProductoDetalleContent(
    modifier: Modifier = Modifier,
    state: ProductoDetalleState,
    onEvent: (ProductoDetalleEvent) -> Unit
) {
    val producto = state.producto ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ProductoDetalleHeader(producto.imagenUrl, producto.nombre)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProductoDetalleCategoria(producto.categoria)
            ProductoDetalleTitulo(producto.nombre)
            ProductoDetallePrecio(producto.precio, producto.precioFormateado)
            HorizontalDivider()
            ProductoDetalleDescripcion(producto.descripcion)
            HorizontalDivider()
            ProductoDetalleCantidad(state.cantidad, onEvent)
            ProductoDetalleSubtotal(producto.precio, state.cantidad)
            ProductoDetalleAddToCartButton(onEvent)
        }
    }
}

@Composable
private fun ProductoDetalleHeader(imageUrl: String, contentDescription: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ProductoDetalleCategoria(categoria: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = categoria,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ProductoDetalleTitulo(nombre: String) {
    Text(
        text = nombre,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ProductoDetallePrecio(precio: Double, precioFormateado: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = precioFormateado,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        if (precio > 50) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.error
            ) {
                Text(
                    "-15%",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

@Composable
private fun ProductoDetalleDescripcion(descripcion: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Descripción",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            descripcion,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProductoDetalleCantidad(
    cantidad: Int,
    onEvent: (ProductoDetalleEvent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Cantidad",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    onEvent(ProductoDetalleEvent.UpdateCantidad(cantidad - 1))
                },
                enabled = cantidad > 1
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Disminuir")
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    cantidad.toString(),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = {
                    onEvent(ProductoDetalleEvent.UpdateCantidad(cantidad + 1))
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aumentar")
            }
        }
    }
}

@Composable
private fun ProductoDetalleSubtotal(precio: Double, cantidad: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Subtotal", style = MaterialTheme.typography.titleMedium)

        Text(
            "$${String.format("%.2f", precio * cantidad)}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ProductoDetalleAddToCartButton(
    onEvent: (ProductoDetalleEvent) -> Unit
) {
    Button(
        onClick = { onEvent(ProductoDetalleEvent.AddToCart) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.ShoppingCart, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Agregar al Carrito",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ErrorWithRetry(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(message)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Volver")
        }
    }
}