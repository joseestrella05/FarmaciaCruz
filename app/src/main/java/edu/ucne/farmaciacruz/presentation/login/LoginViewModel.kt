package edu.ucne.farmaciacruz.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.usecase.login.LoginUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUiEvent {
    data object NavigateToHome : LoginUiEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<LoginUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> handleEmailChanged(event.email)
            is LoginEvent.PasswordChanged -> handlePasswordChanged(event.password)
            is LoginEvent.LoginClicked -> handleLogin()
            is LoginEvent.ClearError -> handleClearError()
        }
    }

    private fun handleEmailChanged(email: String) {
        _state.update { it.copy(email = email) }
    }

    private fun handlePasswordChanged(password: String) {
        _state.update { it.copy(password = password) }
    }

    private fun handleLogin() {
        val currentState = _state.value

        viewModelScope.launch {
            loginUseCase(currentState.email, currentState.password).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }

                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                user = result.data,
                                error = null
                            )
                        }
                        _uiEvent.emit(LoginUiEvent.NavigateToHome)
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleClearError() {
        _state.update { it.copy(error = null) }
    }
}


