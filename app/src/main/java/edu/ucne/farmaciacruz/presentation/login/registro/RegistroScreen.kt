package edu.ucne.farmaciacruz.presentation.login.registro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.ucne.farmaciacruz.R
import edu.ucne.farmaciacruz.ui.theme.onPrimaryContainerDarkHighContrast

@Composable
fun RegistroScreen(
    onRegistroSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: RegistroViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is RegistroEvent.ShowError -> {

                }

                is RegistroEvent.ShowSuccess -> {

                }

                is RegistroEvent.NavigateToHome -> {
                    onRegistroSuccess()
                }
            }
        }
    }

    RegistroContent(
        state = state,
        onNombreChanged = { viewModel.processIntent(RegistroIntent.NombreChanged(it)) },
        onApellidoChanged = { viewModel.processIntent(RegistroIntent.ApellidoChanged(it)) },
        onEmailChanged = { viewModel.processIntent(RegistroIntent.EmailChanged(it)) },
        onTelefonoChanged = { viewModel.processIntent(RegistroIntent.TelefonoChanged(it)) },
        onPasswordChanged = { viewModel.processIntent(RegistroIntent.PasswordChanged(it)) },
        onConfirmarPasswordChanged = { viewModel.processIntent(RegistroIntent.ConfirmarPasswordChanged(it)) },
        onTerminosChanged = { viewModel.processIntent(RegistroIntent.TerminosChanged(it)) },
        onRegistrarClicked = { viewModel.processIntent(RegistroIntent.RegistrarClicked) },
        onBackToLogin = onBackToLogin,
        onClearError = { viewModel.processIntent(RegistroIntent.ClearError) }
    )
}

@Composable
private fun RegistroContent(
    state: RegistroState,
    onNombreChanged: (String) -> Unit,
    onApellidoChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onTelefonoChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmarPasswordChanged: (String) -> Unit,
    onTerminosChanged: (Boolean) -> Unit,
    onRegistrarClicked: () -> Unit,
    onBackToLogin: () -> Unit,
    onClearError: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmarPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(onPrimaryContainerDarkHighContrast)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_farmacia_cruz),
                        contentDescription = "Farmacia Cruz Logo",
                        modifier = Modifier
                            .size(180.dp)


                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Crear Cuenta",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Únete a Farmacia Cruz",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Nombre",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = state.nombre,
                        onValueChange = onNombreChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("José Gabriel") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, null)
                        },
                        singleLine = true,
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    Text(
                        text = "Apellido",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = state.apellido,
                        onValueChange = onApellidoChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Estrella") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, null)
                        },
                        singleLine = true,
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    Text(
                        text = "Correo electrónico",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = onEmailChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("ejemplo@correo.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    Text(
                        text = "Teléfono",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = state.telefono,
                        onValueChange = onTelefonoChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("+1 829 230 1111") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    Text(
                        text = "Contraseña",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = onPasswordChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("········") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    Text(
                        text = "Confirmar contraseña",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = state.confirmarPassword,
                        onValueChange = onConfirmarPasswordChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("········") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null)
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                confirmarPasswordVisible = !confirmarPasswordVisible
                            }) {
                                Icon(
                                    if (confirmarPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    null
                                )
                            }
                        },
                        visualTransformation = if (confirmarPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = state.aceptaTerminos,
                            onCheckedChange = onTerminosChanged,
                            enabled = !state.isLoading
                        )
                        Text(
                            text = "He leído y acepto los términos y condiciones de Farmacia Cruz",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onRegistrarClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Registrarse",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "¿Ya tienes cuenta? ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(
                            onClick = onBackToLogin,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Inicia sesión",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            if (state.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onClearError,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Text(
                                "✕",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegistroScreenPreview() {
    MaterialTheme {
        RegistroContent(
            state = RegistroState(
                nombre = "",
                apellido = "",
                email = "",
                telefono = "",
                password = "",
                confirmarPassword = "",
                aceptaTerminos = false,
                isLoading = false,
                error = null
            ),
            onNombreChanged = {},
            onApellidoChanged = {},
            onEmailChanged = {},
            onTelefonoChanged = {},
            onPasswordChanged = {},
            onConfirmarPasswordChanged = {},
            onTerminosChanged = {},
            onRegistrarClicked = {},
            onBackToLogin = {},
            onClearError = {}
        )
    }
}
