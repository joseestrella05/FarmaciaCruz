package edu.ucne.farmaciacruz.presentation.configuracion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.domain.usecase.login.GetCurrentUserUseCase
import edu.ucne.farmaciacruz.domain.usecase.login.LogoutUseCase
import edu.ucne.farmaciacruz.domain.usecase.preference.GetApiUrlUseCase
import edu.ucne.farmaciacruz.domain.usecase.preference.GetThemeUseCase
import edu.ucne.farmaciacruz.domain.usecase.preference.GetUserIdUseCase
import edu.ucne.farmaciacruz.domain.usecase.preference.SetApiUrlUseCase
import edu.ucne.farmaciacruz.domain.usecase.preference.ToggleThemeUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfiguracionViewModel @Inject constructor(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getApiUrlUseCase: GetApiUrlUseCase,
    private val setApiUrlUseCase: SetApiUrlUseCase,
    private val getThemeUseCase: GetThemeUseCase,
    private val toggleThemeUseCase: ToggleThemeUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ConfiguracionState())
    val state = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ConfiguracionUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        cargarUsuario()
        cargarPreferencias()
    }

    fun onEvent(event: ConfiguracionEvent) {
        when (event) {
            ConfiguracionEvent.LoadUserData -> cargarUsuario()
            ConfiguracionEvent.LoadPreferences -> cargarPreferencias()
            is ConfiguracionEvent.ApiUrlChanged -> actualizarUrl(event.newUrl)
            ConfiguracionEvent.ThemeToggled -> alternarTema()
            ConfiguracionEvent.ShowLogoutDialog -> _state.update {
                it.copy(showLogoutDialog = true)
            }
            ConfiguracionEvent.DismissLogoutDialog -> _state.update {
                it.copy(showLogoutDialog = false)
            }
            ConfiguracionEvent.Logout -> cerrarSesion()
        }
    }

    private fun cargarUsuario() = viewModelScope.launch {
        getCurrentUserUseCase().collect { user ->
            _state.update { it.copy(user = user) }
        }
    }

    private fun cargarPreferencias() = viewModelScope.launch {
        launch {
            getApiUrlUseCase().collect { url ->
                _state.update { it.copy(apiUrl = url) }
            }
        }
        launch {
            getThemeUseCase().collect { isDark ->
                _state.update { it.copy(isDarkTheme = isDark) }
            }
        }
    }

    private fun actualizarUrl(url: String) = viewModelScope.launch {
        setApiUrlUseCase(url)
        _uiEvent.emit(ConfiguracionUiEvent.ShowSuccess("URL actualizada"))
    }

    private fun alternarTema() = viewModelScope.launch {
        toggleThemeUseCase(state.value.isDarkTheme)
    }

    private fun cerrarSesion() = viewModelScope.launch {
        logoutUseCase()
        _uiEvent.emit(ConfiguracionUiEvent.NavigateToLogin)
    }
}