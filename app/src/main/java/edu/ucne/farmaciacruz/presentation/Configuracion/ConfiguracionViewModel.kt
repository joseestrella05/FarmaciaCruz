package edu.ucne.farmaciacruz.presentation.Configuracion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.data.local.PreferencesManager
import edu.ucne.farmaciacruz.domain.usecase.login.GetCurrentUserUseCase
import edu.ucne.farmaciacruz.domain.usecase.login.LogoutUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfiguracionViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ConfiguracionState())
    val state: StateFlow<ConfiguracionState> = _state.asStateFlow()

    private val _event = Channel<ConfiguracionEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        processIntent(ConfiguracionIntent.LoadUserData)
        processIntent(ConfiguracionIntent.LoadPreferences)
    }

    fun processIntent(intent: ConfiguracionIntent) {
        when (intent) {
            is ConfiguracionIntent.LoadUserData -> handleLoadUserData()
            is ConfiguracionIntent.LoadPreferences -> handleLoadPreferences()
            is ConfiguracionIntent.ApiUrlChanged -> handleApiUrlChanged(intent.newUrl)
            is ConfiguracionIntent.ThemeToggled -> handleThemeToggled()
            is ConfiguracionIntent.ShowLogoutDialog -> handleShowLogoutDialog()
            is ConfiguracionIntent.DismissLogoutDialog -> handleDismissLogoutDialog()
            is ConfiguracionIntent.Logout -> handleLogout()
        }
    }

    private fun handleLoadUserData() {
        viewModelScope.launch {
            try {
                getCurrentUserUseCase().collect { user ->
                    _state.update { it.copy(user = user, error = null) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
                _event.send(ConfiguracionEvent.ShowError(e.message ?: "Error al cargar usuario"))
            }
        }
    }

    private fun handleLoadPreferences() {
        viewModelScope.launch {
            try {
                preferencesManager.getApiUrl().collect { url ->
                    _state.update { it.copy(apiUrl = url) }
                }
            } catch (e: Exception) {
                _event.send(ConfiguracionEvent.ShowError("Error al cargar preferencias"))
            }
        }
        viewModelScope.launch {
            try {
                preferencesManager.getThemePreference().collect { isDark ->
                    _state.update { it.copy(isDarkTheme = isDark) }
                }
            } catch (e: Exception) {
                _event.send(ConfiguracionEvent.ShowError("Error al cargar tema"))
            }
        }
    }

    private fun handleApiUrlChanged(newUrl: String) {
        viewModelScope.launch {
            try {
                preferencesManager.saveApiUrl(newUrl)
                _state.update { it.copy(apiUrl = newUrl) }
                _event.send(ConfiguracionEvent.ShowSuccess("URL de API actualizada"))
            } catch (e: Exception) {
                _event.send(ConfiguracionEvent.ShowError("Error al guardar URL"))
            }
        }
    }

    private fun handleThemeToggled() {
        viewModelScope.launch {
            try {
                val newTheme = !_state.value.isDarkTheme
                preferencesManager.saveThemePreference(newTheme)
                _state.update { it.copy(isDarkTheme = newTheme) }

                val message = if (newTheme) "Tema oscuro activado" else "Tema claro activado"
                _event.send(ConfiguracionEvent.ShowSuccess(message))
            } catch (e: Exception) {
                _event.send(ConfiguracionEvent.ShowError("Error al cambiar tema"))
            }
        }
    }

    private fun handleShowLogoutDialog() {
        _state.update { it.copy(showLogoutDialog = true) }
    }

    private fun handleDismissLogoutDialog() {
        _state.update { it.copy(showLogoutDialog = false) }
    }

    private fun handleLogout() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                logoutUseCase()

                _state.update {
                    it.copy(
                        isLoading = false,
                        showLogoutDialog = false
                    )
                }

                _event.send(ConfiguracionEvent.NavigateToLogin)
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _event.send(ConfiguracionEvent.ShowError("Error al cerrar sesión"))
            }
        }
    }
}