package edu.ucne.farmaciacruz.presentation.login.recoverypassword


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.usecase.login.RecoveryPasswordUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecuperarPasswordViewModel @Inject constructor(
    private val recoveryPasswordUseCase: RecoveryPasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecuperarPasswordState())
    val state = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<RecuperarPasswordUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onEvent(event: RecuperarPasswordEvent) {
        when (event) {
            is RecuperarPasswordEvent.EmailChanged ->
                _state.update { it.copy(email = event.email, error = null) }

            RecuperarPasswordEvent.EnviarClicked -> enviar()
            RecuperarPasswordEvent.VolverLogin -> viewModelScope.launch {
                _uiEvent.emit(RecuperarPasswordUiEvent.NavigateToLogin)
            }
            RecuperarPasswordEvent.ClearError -> _state.update { it.copy(error = null) }
        }
    }

    private fun enviar() = viewModelScope.launch {
        recoveryPasswordUseCase(_state.value.email).collect { result ->
            when (result) {
                is Resource.Loading ->
                    _state.update { it.copy(isLoading = true, error = null) }

                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, emailEnviado = true) }
                    _uiEvent.emit(
                        RecuperarPasswordUiEvent.ShowSuccess(
                            "Te enviamos un enlace para restablecer tu contraseña."
                        )
                    )
                }

                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                    _uiEvent.emit(
                        RecuperarPasswordUiEvent.ShowError(result.message ?: "Error")
                    )
                }
            }
        }
    }
}