package edu.ucne.farmaciacruz.presentation.admin.ordenes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.ucne.farmaciacruz.domain.model.OrderAdmin
import edu.ucne.farmaciacruz.domain.model.OrderStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdenesScreen(
    onBack: () -> Unit,
    viewModel: AdminOrdenesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AdminOrdenesEffect.ShowError ->
                    snackbarHostState.showSnackbar(effect.message)
                is AdminOrdenesEffect.ShowSuccess ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Órdenes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(AdminOrdenesEvent.Refresh) }
                    ) {
                        Icon(Icons.Default.Refresh, "Actualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barra de búsqueda
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.onEvent(AdminOrdenesEvent.SearchQueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Filtros por estado
            EstadoFilterChips(
                selectedEstado = state.selectedEstado,
                estados = state.estados,
                onEstadoSelected = { viewModel.onEvent(AdminOrdenesEvent.EstadoFilterSelected(it)) }
            )

            // Resumen rápido
            if (state.ordenes.isNotEmpty()) {
                ResumenOrdenes(ordenes = state.ordenes)
            }

            // Contador de resultados
            Text(
                text = "${state.ordenesFiltradas.size} órdenes encontradas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Lista de órdenes
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    state.error != null -> {
                        ErrorContent(
                            message = state.error!!,
                            onRetry = { viewModel.onEvent(AdminOrdenesEvent.LoadOrdenes) },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    state.ordenesFiltradas.isEmpty() -> {
                        EmptyContent(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.ordenesFiltradas,
                                key = { it.orderId }
                            ) { orden ->
                                OrdenCard(
                                    orden = orden,
                                    onVerDetalle = {
                                        viewModel.onEvent(
                                            AdminOrdenesEvent.ShowDetalleDialog(orden)
                                        )
                                    },
                                    onCambiarEstado = {
                                        viewModel.onEvent(
                                            AdminOrdenesEvent.ShowChangeStatusDialog(orden)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogos
    if (state.showChangeStatusDialog && state.ordenSeleccionada != null) {
        ChangeStatusDialog(
            orden = state.ordenSeleccionada!!,
            estados = state.estados,
            isLoading = state.isLoading,
            onDismiss = { viewModel.onEvent(AdminOrdenesEvent.DismissDialogs) },
            onConfirm = { nuevoEstado ->
                viewModel.onEvent(
                    AdminOrdenesEvent.ChangeStatus(
                        state.ordenSeleccionada!!.orderId,
                        nuevoEstado
                    )
                )
            }
        )
    }

    if (state.showDetalleDialog && state.ordenSeleccionada != null) {
        DetalleOrdenDialog(
            orden = state.ordenSeleccionada!!,
            onDismiss = { viewModel.onEvent(AdminOrdenesEvent.DismissDialogs) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Buscar por ID o cliente...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    )
}

@Composable
private fun EstadoFilterChips(
    selectedEstado: OrderStatus?,
    estados: List<OrderStatus>,
    onEstadoSelected: (OrderStatus?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedEstado == null,
                onClick = { onEstadoSelected(null) },
                label = { Text("Todos") },
                leadingIcon = if (selectedEstado == null) {
                    { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                } else null
            )
        }

        items(estados) { estado ->
            FilterChip(
                selected = selectedEstado == estado,
                onClick = { onEstadoSelected(if (selectedEstado == estado) null else estado) },
                label = { Text(getEstadoDisplayName(estado)) },
                leadingIcon = if (selectedEstado == estado) {
                    { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = getEstadoColor(estado).copy(alpha = 0.2f)
                )
            )
        }
    }
}

@Composable
private fun ResumenOrdenes(ordenes: List<OrderAdmin>) {
    val pendientes = ordenes.count { it.estado == OrderStatus.PENDIENTE }
    val procesando = ordenes.count { it.estado == OrderStatus.PROCESANDO }
    val completados = ordenes.count { it.estado == OrderStatus.COMPLETADO }
    val fallidos = ordenes.count { it.estado == OrderStatus.FALLIDO }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ResumenItem(
            count = pendientes,
            label = "Pendientes",
            color = getEstadoColor(OrderStatus.PENDIENTE)
        )
        ResumenItem(
            count = procesando,
            label = "Procesando",
            color = getEstadoColor(OrderStatus.PROCESANDO)
        )
        ResumenItem(
            count = completados,
            label = "Completados",
            color = getEstadoColor(OrderStatus.COMPLETADO)
        )
        ResumenItem(
            count = fallidos,
            label = "Fallidos",
            color = getEstadoColor(OrderStatus.FALLIDO)
        )
    }
}

@Composable
private fun ResumenItem(
    count: Int,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OrdenCard(
    orden: OrderAdmin,
    onVerDetalle: () -> Unit,
    onCambiarEstado: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Orden #${orden.orderId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = orden.usuarioNombre,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                EstadoBadge(estado = orden.estado)
            }

            Spacer(Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(Modifier.height(12.dp))

            // Información de la orden
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    InfoRow(
                        icon = Icons.Default.ShoppingCart,
                        text = "${orden.cantidadProductos} productos"
                    )
                    Spacer(Modifier.height(4.dp))
                    InfoRow(
                        icon = Icons.Default.Payment,
                        text = orden.metodoPago
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = orden.totalFormateado,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = orden.fechaCreacion.take(10),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(Modifier.height(8.dp))

            // Acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onVerDetalle) {
                    Icon(Icons.Default.Visibility, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ver Detalle")
                }

                TextButton(onClick = onCambiarEstado) {
                    Icon(Icons.Default.Edit, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Cambiar Estado")
                }
            }
        }
    }
}

@Composable
private fun EstadoBadge(estado: OrderStatus) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = getEstadoColor(estado).copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getEstadoIcon(estado),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = getEstadoColor(estado)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = getEstadoDisplayName(estado),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = getEstadoColor(estado)
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChangeStatusDialog(
    orden: OrderAdmin,
    estados: List<OrderStatus>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedEstado by remember { mutableStateOf(orden.estado) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar Estado") },
        text = {
            Column {
                Text("Orden #${orden.orderId}")
                Text(
                    "Cliente: ${orden.usuarioNombre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                Text("Selecciona el nuevo estado:")

                Spacer(Modifier.height(8.dp))

                estados.forEach { estado ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedEstado == estado,
                            onClick = { selectedEstado = estado }
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = getEstadoIcon(estado),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = getEstadoColor(estado)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(getEstadoDisplayName(estado))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedEstado.name) },
                enabled = !isLoading && selectedEstado != orden.estado
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun DetalleOrdenDialog(
    orden: OrderAdmin,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Orden #${orden.orderId}")
                EstadoBadge(estado = orden.estado)
            }
        },
        text = {
            Column {
                DetailRow(label = "Cliente", value = orden.usuarioNombre)
                DetailRow(label = "Total", value = orden.totalFormateado)
                DetailRow(label = "Productos", value = "${orden.cantidadProductos} items")
                DetailRow(label = "Método de Pago", value = orden.metodoPago)
                DetailRow(label = "Fecha Creación", value = orden.fechaCreacion.take(19).replace("T", " "))
                DetailRow(label = "Última Actualización", value = orden.fechaActualizacion.take(19).replace("T", " "))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun getEstadoColor(estado: OrderStatus): Color {
    return when (estado) {
        OrderStatus.PENDIENTE -> Color(0xFFFF9800)
        OrderStatus.PROCESANDO -> Color(0xFF2196F3)
        OrderStatus.COMPLETADO -> Color(0xFF4CAF50)
        OrderStatus.FALLIDO -> Color(0xFFF44336)
        OrderStatus.CANCELADO -> Color(0xFF9E9E9E)
    }
}

private fun getEstadoDisplayName(estado: OrderStatus): String {
    return when (estado) {
        OrderStatus.PENDIENTE -> "Pendiente"
        OrderStatus.PROCESANDO -> "Procesando"
        OrderStatus.COMPLETADO -> "Completado"
        OrderStatus.FALLIDO -> "Fallido"
        OrderStatus.CANCELADO -> "Cancelado"
    }
}

private fun getEstadoIcon(estado: OrderStatus): androidx.compose.ui.graphics.vector.ImageVector {
    return when (estado) {
        OrderStatus.PENDIENTE -> Icons.Default.Schedule
        OrderStatus.PROCESANDO -> Icons.Default.Sync
        OrderStatus.COMPLETADO -> Icons.Default.CheckCircle
        OrderStatus.FALLIDO -> Icons.Default.Error
        OrderStatus.CANCELADO -> Icons.Default.Cancel
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

@Composable
private fun EmptyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Inbox,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No se encontraron órdenes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}