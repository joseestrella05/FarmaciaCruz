package edu.ucne.farmaciacruz.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.ucne.farmaciacruz.presentation.login.LoginIntent.*
import edu.ucne.farmaciacruz.presentation.login.LoginEvent.*
import edu.ucne.farmaciacruz.ui.theme.onPrimaryContainerDarkHighContrast
import edu.ucne.farmaciacruz.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegistroClick: () -> Unit = {},
    onOlvidoPasswordClick: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is NavigateToHome -> onLoginSuccess()
                is ShowError -> {}
            }
        }
    }

    LoginScreenContent(
        state = state,
        isLoading = state.isLoading,
        passwordVisible = passwordVisible,
        onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
        onEmailChange = { viewModel.processIntent(EmailChanged(it)) },
        onPasswordChange = { viewModel.processIntent(PasswordChanged(it)) },
        onLoginClick = { viewModel.processIntent(LoginClicked) },
        onRegistroClick = onRegistroClick,
        onOlvidoPasswordClick = onOlvidoPasswordClick,
        onClearError = { viewModel.processIntent(ClearError) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    state: LoginState,
    isLoading: Boolean,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegistroClick: () -> Unit,
    onOlvidoPasswordClick: () -> Unit,
    onClearError: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(onPrimaryContainerDarkHighContrast)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_farmacia_cruz),
                contentDescription = "Farmacia Cruz Logo",
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.CenterHorizontally)

            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tu salud es nuestra prioridad",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)
                ) {

                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = onEmailChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "example@email.com",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        singleLine = true,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "At least 8 characters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = onPasswordVisibilityChange) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        singleLine = true,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onOlvidoPasswordClick,
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Forgot Password?",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading
                                && state.email.isNotBlank()
                                && state.password.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Sign In",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(
                    onClick = onRegistroClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Sign up",
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
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
                                color = MaterialTheme.colorScheme.onErrorContainer
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
fun LoginScreenPreview() {
    val previewState = LoginState(
        email = "example@email.com",
        password = "123456",
        isLoading = false,
        error = null
    )

    MaterialTheme {
        LoginScreenContent(
            state = previewState,
            isLoading = false,
            passwordVisible = false,
            onPasswordVisibilityChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onRegistroClick = {},
            onOlvidoPasswordClick = {},
            onClearError = {}
        )
    }
}
