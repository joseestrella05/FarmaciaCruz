package edu.ucne.farmaciacruz.presentation.login.recoverypassword

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.data.remote.api.ApiService
import edu.ucne.farmaciacruz.data.remote.request.RecoveryRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecuperarPasswordViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(RecuperarPasswordState())
    val state: StateFlow<RecuperarPasswordState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<RecuperarPasswordUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onEvent(event: RecuperarPasswordEvent) {
        when (event) {
            is RecuperarPasswordEvent.EmailChanged -> handleEmailChanged(event.email)
            is RecuperarPasswordEvent.EnviarClicked -> handleEnviarEmail()
            is RecuperarPasswordEvent.VolverLogin -> handleVolverLogin()
            is RecuperarPasswordEvent.ClearError -> handleClearError()
        }
    }

    private fun handleEmailChanged(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }

    private fun handleEnviarEmail() {
        val currentState = _state.value

        // Validación básica
        if (currentState.email.isBlank()) {
            _state.update { it.copy(error = "Por favor ingresa tu correo electrónico") }
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _state.update { it.copy(error = "Por favor ingresa un correo válido") }
            return
        }

        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }

                val response = apiService.RecoveryPassword(
                    RecoveryRequest(email = currentState.email)
                )

                if (response.isSuccessful) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            emailEnviado = true,
                            error = null
                        )
                    }
                    _uiEvent.emit(
                        RecuperarPasswordUiEvent.ShowSuccess(
                            "Te enviamos un enlace para restablecer tu contraseña. Revisa tu correo."
                        )
                    )
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "No se pudo enviar el correo. Intenta nuevamente."
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Error de conexión. Verifica tu internet."
                    )
                }
                _uiEvent.emit(
                    RecuperarPasswordUiEvent.ShowError(
                        e.message ?: "Error desconocido"
                    )
                )
            }
        }
    }

    private fun handleVolverLogin() {
        viewModelScope.launch {
            _uiEvent.emit(RecuperarPasswordUiEvent.NavigateToLogin)
        }
    }

    private fun handleClearError() {
        _state.update { it.copy(error = null) }
    }
}