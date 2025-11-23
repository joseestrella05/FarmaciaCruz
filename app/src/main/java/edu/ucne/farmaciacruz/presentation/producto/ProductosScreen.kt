package edu.ucne.farmaciacruz.presentation.producto

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import edu.ucne.farmaciacruz.domain.model.Producto
import edu.ucne.farmaciacruz.presentation.carrito.CarritoBottomSheet
import edu.ucne.farmaciacruz.ui.theme.FarmaciaCruzTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    onProductoClick: (Int) -> Unit,
    onConfigClick: () -> Unit,
    viewModel: ProductosViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCarritoSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ProductosUiEvent.ShowError ->
                    snackbarHostState.showSnackbar(event.message)
                is ProductosUiEvent.ShowSuccess ->
                    snackbarHostState.showSnackbar(event.message)
                is ProductosUiEvent.NavigateToDetail ->
                    onProductoClick(event.productoId)
            }
        }
    }

    if (showCarritoSheet) {
        CarritoBottomSheet(
            carrito = state.carrito,
            total = state.carrito.sumOf { it.producto.precio * it.cantidad },
            onDismiss = { showCarritoSheet = false },
            onUpdateQuantity = { productoId, cantidad ->
                viewModel.onEvent(ProductosEvent.UpdateQuantity(productoId, cantidad))
            },
            onRemoveItem = { productoId ->
                viewModel.onEvent(ProductosEvent.RemoveFromCart(productoId))
            },
            onProceedToCheckout = {
                showCarritoSheet = false
            }
        )
    }

    ProductosScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        cantidadCarrito = state.carrito.sumOf { it.cantidad },
        onEvent = viewModel::onEvent,
        onCarritoClick = { showCarritoSheet = true },
        onConfigClick = onConfigClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductosScreenContent(
    state: ProductosState,
    snackbarHostState: SnackbarHostState,
    cantidadCarrito: Int,
    onEvent: (ProductosEvent) -> Unit,
    onCarritoClick: () -> Unit,
    onConfigClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Bienvenido",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Notifications,
                                    contentDescription = "Notificaciones",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            BadgedBox(
                                badge = {
                                    if (cantidadCarrito > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error
                                        ) {
                                            Text(cantidadCarrito.toString())
                                        }
                                    }
                                }
                            ) {
                                IconButton(
                                    onClick = onCarritoClick,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.ShoppingCart,
                                        contentDescription = "Carrito",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { onEvent(ProductosEvent.SearchQueryChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Buscar productos…")
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onEvent(ProductosEvent.SearchQueryChanged("")) }
                                ) {
                                    Icon(
                                        Icons.Filled.Clear,
                                        contentDescription = "Limpiar",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        bottomBar = {
            BottomBarManual(
                onHome = {},
                onSearch = {},
                onCart = onCarritoClick,
                onProfile = onConfigClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = state.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Button(onClick = { onEvent(ProductosEvent.LoadProductos) }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                QuickAccessCard(
                                    icon = Icons.Outlined.MedicalServices,
                                    title = "Mis Pedidos",
                                    onClick = { }
                                )
                                QuickAccessCard(
                                    icon = Icons.Outlined.History,
                                    title = "Historial",
                                    onClick = { }
                                )
                                QuickAccessCard(
                                    icon = Icons.Outlined.FavoriteBorder,
                                    title = "Favoritos",
                                    onClick = { }
                                )
                            }
                        }

                        if (state.categorias.isNotEmpty()) {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Categorías",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilterChip(
                                            selected = state.selectedCategoria == null,
                                            onClick = { onEvent(ProductosEvent.CategoriaSelected(null)) },
                                            label = { Text("Todas") }
                                        )

                                        state.categorias.take(3).forEach { categoria ->
                                            FilterChip(
                                                selected = state.selectedCategoria == categoria,
                                                onClick = { onEvent(ProductosEvent.CategoriaSelected(categoria)) },
                                                label = { Text(categoria) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Productos Disponibles",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${state.productosFiltrados.size} productos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (state.productosFiltrados.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.SearchOff,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "No se encontraron productos",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(state.productosFiltrados) { producto ->
                                ProductoCard(
                                    producto = producto,
                                    onClick = { onEvent(ProductosEvent.ProductoClicked(producto.id)) },
                                    onAddToCart = { onEvent(ProductosEvent.AddToCart(producto)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBarManual(
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onCart: () -> Unit,
    onProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomBarItem(Icons.Outlined.Home, "Inicio", true, onHome)
        BottomBarItem(Icons.Outlined.Search, "Buscar", false, onSearch)
        BottomBarItem(Icons.Outlined.ShoppingCart, "Carrito", false, onCart)
        BottomBarItem(Icons.Outlined.Person, "Perfil", false, onProfile)
    }
}

@Composable
fun BottomBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(26.dp)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuickAccessCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(100.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ProductoCard(
    producto: Producto,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                AsyncImage(
                    model = producto.imagenUrl,
                    contentDescription = producto.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = producto.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = producto.precioFormateado,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (producto.precio > 50) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                text = "-15%",
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
            }

            FilledIconButton(
                onClick = onAddToCart,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = "Agregar",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ===== PREVIEWS =====

@Preview(name = "Productos Screen - Light", showBackground = true)
@Composable
private fun ProductosScreenPreview() {
    val sampleProductos = listOf(
        Producto(
            id = 1,
            nombre = "Paracetamol 500mg",
            descripcion = "Analgésico y antipirético",
            precio = 45.00,
            imagenUrl = "",
            categoria = "Medicamentos"
        ),
        Producto(
            id = 2,
            nombre = "Ibuprofeno 400mg",
            descripcion = "Antiinflamatorio",
            precio = 65.00,
            imagenUrl = "",
            categoria = "Medicamentos"
        ),
        Producto(
            id = 3,
            nombre = "Vitamina C 1000mg",
            descripcion = "Suplemento vitamínico",
            precio = 35.00,
            imagenUrl = "",
            categoria = "Vitaminas"
        )
    )

    FarmaciaCruzTheme {
        ProductosScreenContent(
            state = ProductosState(
                productos = sampleProductos,
                productosFiltrados = sampleProductos,
                categorias = listOf("Medicamentos", "Vitaminas"),
                selectedCategoria = null,
                searchQuery = "",
                isLoading = false,
                error = null
            ),
            snackbarHostState = remember { SnackbarHostState() },
            cantidadCarrito = 3,
            onEvent = {},
            onCarritoClick = {},
            onConfigClick = {}
        )
    }
}

@Preview(name = "Productos Screen - Dark", showBackground = true)
@Composable
private fun ProductosScreenPreviewDark() {
    val sampleProductos = listOf(
        Producto(
            id = 1,
            nombre = "Paracetamol 500mg",
            descripcion = "Analgésico y antipirético",
            precio = 45.00,
            imagenUrl = "",
            categoria = "Medicamentos"
        ),
        Producto(
            id = 2,
            nombre = "Ibuprofeno 400mg",
            descripcion = "Antiinflamatorio",
            precio = 65.00,
            imagenUrl = "",
            categoria = "Medicamentos"
        )
    )

    FarmaciaCruzTheme(darkTheme = true) {
        ProductosScreenContent(
            state = ProductosState(
                productos = sampleProductos,
                productosFiltrados = sampleProductos,
                categorias = listOf("Medicamentos", "Vitaminas"),
                selectedCategoria = null,
                searchQuery = "",
                isLoading = false,
                error = null
            ),
            snackbarHostState = remember { SnackbarHostState() },
            cantidadCarrito = 0,
            onEvent = {},
            onCarritoClick = {},
            onConfigClick = {}
        )
    }
}

@Preview(name = "Productos Screen - Loading", showBackground = true)
@Composable
private fun ProductosScreenLoadingPreview() {
    FarmaciaCruzTheme {
        ProductosScreenContent(
            state = ProductosState(
                isLoading = true
            ),
            snackbarHostState = remember { SnackbarHostState() },
            cantidadCarrito = 0,
            onEvent = {},
            onCarritoClick = {},
            onConfigClick = {}
        )
    }
}