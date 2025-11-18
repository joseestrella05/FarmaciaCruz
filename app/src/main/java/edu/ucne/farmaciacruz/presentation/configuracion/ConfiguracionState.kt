package edu.ucne.farmaciacruz.presentation.configuracion

import edu.ucne.farmaciacruz.domain.model.User

data class ConfiguracionState(
    val user: User? = null,
    val apiUrl: String = "",
    val isDarkTheme: Boolean = false,
    val isLoading: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val error: String? = null
)