package edu.ucne.farmaciacruz.presentation.configuracion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    viewModel: ConfiguracionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showApiDialog by remember { mutableStateOf(false) }
    var tempApiUrl by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ConfiguracionUiEvent.ShowError ->
                    snackbarHostState.showSnackbar(event.message)

                is ConfiguracionUiEvent.ShowSuccess ->
                    snackbarHostState.showSnackbar(event.message)

                is ConfiguracionUiEvent.NavigateToLogin ->
                    onLogout()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { ConfigTopBar(onBack = onBack) }
    ) { padding ->

        state.error?.let { errorMessage ->
            ErrorWithRetry(
                message = errorMessage,
                onRetry = { onBack() }
            )
            return@Scaffold
        }

        Box(Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                UserCardSection(state)

                Spacer(Modifier.height(24.dp))

                // Sección de Administrador - Solo visible para administradores
                if (state.user?.rol == "Administrador") {
                    AdminSection(onAdminClick = onNavigateToAdmin)
                    Spacer(Modifier.height(24.dp))
                }

                ApiConfigSection(
                    apiUrl = state.apiUrl,
                    onClick = {
                        tempApiUrl = state.apiUrl
                        showApiDialog = true
                    }
                )

                Spacer(Modifier.height(24.dp))

                AparienciaSection(
                    isDark = state.isDarkTheme,
                    onToggle = { viewModel.onEvent(ConfiguracionEvent.ThemeToggled) }
                )

                Spacer(Modifier.height(24.dp))

                SesionSection(
                    onLogoutClick = {
                        viewModel.onEvent(ConfiguracionEvent.ShowLogoutDialog)
                    }
                )

                Spacer(Modifier.height(32.dp))

                FooterSection()
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (showApiDialog) {
        ApiDialog(
            tempApiUrl = tempApiUrl,
            onValueChange = { tempApiUrl = it },
            onDismiss = { showApiDialog = false },
            onSave = {
                viewModel.onEvent(ConfiguracionEvent.ApiUrlChanged(tempApiUrl))
                showApiDialog = false
            }
        )
    }

    if (state.showLogoutDialog) {
        LogoutDialog(
            isLoading = state.isLoading,
            onDismiss = { viewModel.onEvent(ConfiguracionEvent.DismissLogoutDialog) },
            onConfirm = { viewModel.onEvent(ConfiguracionEvent.Logout) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text("Configuración") },
        navigationIcon = {
            IconButton(onClick = onBack) {
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

@Composable
private fun UserCardSection(state: ConfiguracionState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = state.user?.nombreCompleto ?: "Cargando...",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = state.user?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(8.dp))

                AssistChip(
                    onClick = {},
                    label = { Text(state.user?.rol ?: "") },
                    leadingIcon = {
                        Icon(Icons.Default.Badge, null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }
    }
}

@Composable
private fun AdminSection(onAdminClick: () -> Unit) {
    SectionHeader("Administración")
    Card(
        onClick = onAdminClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        ListItem(
            headlineContent = {
                Text(
                    "Panel de Administración",
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            },
            supportingContent = {
                Text(
                    "Gestionar productos, usuarios y órdenes",
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            },
            leadingContent = {
                Icon(
                    Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(32.dp)
                )
            },
            trailingContent = {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        )
    }
}

@Composable
private fun ApiConfigSection(apiUrl: String, onClick: () -> Unit) {
    SectionHeader("Configuración de API")
    Card(onClick = onClick) {
        ListItem(
            headlineContent = { Text("URL de la API") },
            supportingContent = {
                Text(
                    text = apiUrl,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingContent = {
                Icon(Icons.Default.Cloud, null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingContent = {
                Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
            }
        )
    }
}

@Composable
private fun AparienciaSection(isDark: Boolean, onToggle: () -> Unit) {
    SectionHeader("Apariencia")
    Card {
        ListItem(
            headlineContent = { Text("Tema Oscuro") },
            supportingContent = {
                Text(
                    if (isDark) "Modo oscuro activado" else "Modo claro activado",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingContent = {
                Icon(
                    if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                Switch(checked = isDark, onCheckedChange = { onToggle() })
            }
        )
    }
}

@Composable
private fun SesionSection(onLogoutClick: () -> Unit) {
    SectionHeader("Sesión")
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        onClick = onLogoutClick
    ) {
        ListItem(
            headlineContent = {
                Text(
                    "Cerrar Sesión",
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            },
            supportingContent = {
                Text(
                    "Salir de tu cuenta",
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            },
            leadingContent = {
                Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.error)
            }
        )
    }
}

@Composable
private fun FooterSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))
        Text(
            "Farmacia Cruz App",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            "Versión 1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun ErrorWithRetry(message: String, onRetry: () -> Unit) {
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

@Composable
private fun ApiDialog(
    tempApiUrl: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Cloud, null) },
        title = { Text("Cambiar URL de la API") },
        text = {
            Column {
                OutlinedTextField(
                    value = tempApiUrl,
                    onValueChange = onValueChange,
                    label = { Text("URL") },
                    leadingIcon = { Icon(Icons.Default.Link, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onSave, enabled = tempApiUrl.isNotBlank()) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun LogoutDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Cerrar Sesión") },
        text = {
            Text("¿Estás seguro que deseas cerrar sesión? Tendrás que volver a iniciar sesión.")
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
                    Text("Cerrar Sesión")
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