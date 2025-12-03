package edu.ucne.farmaciacruz.presentation.admin.usuarios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.ucne.farmaciacruz.domain.model.UsuarioAdmin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsuariosScreen(
    onBack: () -> Unit,
    viewModel: AdminUsuariosViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AdminUsuariosUiEvent.ShowError ->
                    snackbarHostState.showSnackbar(event.message)
                is AdminUsuariosUiEvent.ShowSuccess ->
                    snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(AdminUsuariosEvent.Refresh) }
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
        AdminUsuariosContent(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            onEvent = viewModel::onEvent
        )
    }

    if (state.showEditRolDialog && state.usuarioSeleccionado != null) {
        CambiarRolDialog(
            usuario = state.usuarioSeleccionado!!,
            roles = state.roles,
            isLoading = state.isLoading,
            onDismiss = { viewModel.onEvent(AdminUsuariosEvent.DismissDialogs) },
            onConfirm = { nuevoRol ->
                viewModel.onEvent(
                    AdminUsuariosEvent.CambiarRol(
                        state.usuarioSeleccionado!!.usuarioId,
                        nuevoRol
                    )
                )
            }
        )
    }

    if (state.showToggleEstadoDialog && state.usuarioSeleccionado != null) {
        ToggleEstadoDialog(
            usuario = state.usuarioSeleccionado!!,
            isLoading = state.isLoading,
            onDismiss = { viewModel.onEvent(AdminUsuariosEvent.DismissDialogs) },
            onConfirm = {
                viewModel.onEvent(
                    AdminUsuariosEvent.ToggleEstado(
                        state.usuarioSeleccionado!!.usuarioId,
                        !state.usuarioSeleccionado!!.activo
                    )
                )
            }
        )
    }

    if (state.showDeleteDialog && state.usuarioSeleccionado != null) {
        DeleteUsuarioDialog(
            usuario = state.usuarioSeleccionado!!,
            isLoading = state.isLoading,
            onDismiss = { viewModel.onEvent(AdminUsuariosEvent.DismissDialogs) },
            onConfirm = { viewModel.onEvent(AdminUsuariosEvent.ConfirmDelete) }
        )
    }
}

@Composable
private fun AdminUsuariosContent(
    state: AdminUsuariosState,
    modifier: Modifier = Modifier,
    onEvent: (AdminUsuariosUiEvent) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        SearchBar(
            query = state.searchQuery,
            onQueryChange = { onEvent(AdminUsuariosEvent.SearchQueryChanged(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        FilterChipsRow(
            selectedRol = state.selectedRol,
            selectedEstado = state.selectedEstado,
            roles = state.roles,
            onRolSelected = { onEvent(AdminUsuariosEvent.RolFilterSelected(it)) },
            onEstadoSelected = { onEvent(AdminUsuariosEvent.EstadoFilterSelected(it)) }
        )

        Text(
            text = "${state.usuariosFiltrados.size} usuarios encontrados",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

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
                        onRetry = { onEvent(AdminUsuariosEvent.LoadUsuarios) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.usuariosFiltrados.isEmpty() -> {
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
                            items = state.usuariosFiltrados,
                            key = { it.usuarioId }
                        ) { usuario ->
                            UsuarioCard(
                                usuario = usuario,
                                onCambiarRol = {
                                    onEvent(
                                        AdminUsuariosEvent.ShowCambiarRolDialog(usuario)
                                    )
                                },
                                onToggleEstado = {
                                    onEvent(
                                        AdminUsuariosEvent.ShowToggleEstadoDialog(usuario)
                                    )
                                },
                                onDelete = {
                                    onEvent(
                                        AdminUsuariosEvent.ShowDeleteDialog(usuario)
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
        placeholder = { Text("Buscar por nombre o email...") },
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
private fun FilterChipsRow(
    selectedRol: String?,
    selectedEstado: Boolean?,
    roles: List<String>,
    onRolSelected: (String?) -> Unit,
    onEstadoSelected: (Boolean?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedRol == null && selectedEstado == null,
                onClick = {
                    onRolSelected(null)
                    onEstadoSelected(null)
                },
                label = { Text("Todos") },
                leadingIcon = if (selectedRol == null && selectedEstado == null) {
                    { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                } else null
            )
        }

        items(roles) { rol ->
            FilterChip(
                selected = selectedRol == rol,
                onClick = { onRolSelected(if (selectedRol == rol) null else rol) },
                label = { Text(rol) },
                leadingIcon = if (selectedRol == rol) {
                    { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                } else null
            )
        }

        item {
            FilterChip(
                selected = selectedEstado == true,
                onClick = { onEstadoSelected(if (selectedEstado == true) null else true) },
                label = { Text("Activos") },
                leadingIcon = if (selectedEstado == true) {
                    { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                } else null
            )
        }

        item {
            FilterChip(
                selected = selectedEstado == false,
                onClick = { onEstadoSelected(if (selectedEstado == false) null else false) },
                label = { Text("Inactivos") },
                leadingIcon = if (selectedEstado == false) {
                    { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

@Composable
private fun UsuarioCard(
    usuario: UsuarioAdmin,
    onCambiarRol: () -> Unit,
    onToggleEstado: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = usuario.nombre.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = usuario.nombreCompleto,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = usuario.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (usuario.activo)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (usuario.activo) "Activo" else "Inactivo",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (usuario.activo)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text(usuario.rol) },
                    leadingIcon = {
                        Icon(
                            if (usuario.rol == "Administrador")
                                Icons.Default.AdminPanelSettings
                            else
                                Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                if (usuario.emailConfirmado) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Email verificado") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                usuario.telefono?.let { telefono ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = telefono,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Creado: ${usuario.fechaCreacion.take(10)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCambiarRol) {
                    Icon(Icons.Default.Edit, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Rol")
                }

                TextButton(onClick = onToggleEstado) {
                    Icon(
                        if (usuario.activo) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = null,
                        Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (usuario.activo) "Desactivar" else "Activar")
                }

                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
private fun CambiarRolDialog(
    usuario: UsuarioAdmin,
    roles: List<String>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedRol by remember { mutableStateOf(usuario.rol) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar Rol") },
        text = {
            Column {
                Text("Usuario: ${usuario.nombreCompleto}")
                Text(
                    "Email: ${usuario.email}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                Text("Selecciona el nuevo rol:")

                Spacer(Modifier.height(8.dp))

                roles.forEach { rol ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedRol == rol,
                            onClick = { selectedRol = rol }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(rol)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedRol) },
                enabled = !isLoading && selectedRol != usuario.rol
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
private fun ToggleEstadoDialog(
    usuario: UsuarioAdmin,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val accion = if (usuario.activo) "desactivar" else "activar"

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                if (usuario.activo) Icons.Default.Block else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (usuario.activo)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("${accion.replaceFirstChar { it.uppercase() }} Usuario") },
        text = {
            Column {
                Text("¿Estás seguro de que deseas $accion a este usuario?")

                Spacer(Modifier.height(12.dp))

                Text(
                    text = usuario.nombreCompleto,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = usuario.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (usuario.activo) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "El usuario no podrá iniciar sesión mientras esté desactivado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = if (usuario.activo) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(accion.replaceFirstChar { it.uppercase() })
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
private fun DeleteUsuarioDialog(
    usuario: UsuarioAdmin,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Eliminar Usuario") },
        text = {
            Column {
                Text("¿Estás seguro de que deseas eliminar este usuario? Esta acción no se puede deshacer.")

                Spacer(Modifier.height(12.dp))

                Text(
                    text = usuario.nombreCompleto,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = usuario.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Text("Eliminar")
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
            Icons.Default.PersonOff,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No se encontraron usuarios",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun AdminUsuariosScreenPreview() {
    val fakeUsuario = UsuarioAdmin(
        usuarioId = 1,
        nombre = "Juan",
        email = "juan@test.com",
        rol = "Administrador",
        activo = true,
        emailConfirmado = true,
        telefono = "809-555-5555",
        fechaCreacion = "",
        apellido = "",
        ultimoAcceso = ""

    )

    val previewState = AdminUsuariosState(
        usuarios = listOf(fakeUsuario),
        usuariosFiltrados = listOf(fakeUsuario),
        roles = listOf("Administrador", "Cliente")
    )

    MaterialTheme {
        AdminUsuariosContent(
            state = previewState,
            onEvent = {}
        )
    }
}
