package edu.ucne.farmaciacruz.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.usecase.login.LoginUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _event = Channel<LoginEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun processIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged -> handleEmailChanged(intent.email)
            is LoginIntent.PasswordChanged -> handlePasswordChanged(intent.password)
            is LoginIntent.LoginClicked -> handleLogin()
            is LoginIntent.ClearError -> handleClearError()
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
                        _event.send(LoginEvent.NavigateToHome)
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        _event.send(LoginEvent.ShowError(result.message ?: "Error desconocido"))
                    }
                }
            }
        }
    }

    private fun handleClearError() {
        _state.update { it.copy(error = null) }
    }
}