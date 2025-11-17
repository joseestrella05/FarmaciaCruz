package edu.ucne.farmaciacruz.presentation.Configuracion

sealed class ConfiguracionIntent {
    object LoadUserData : ConfiguracionIntent()
    object LoadPreferences : ConfiguracionIntent()
    data class ApiUrlChanged(val newUrl: String) : ConfiguracionIntent()
    object ThemeToggled : ConfiguracionIntent()
    object ShowLogoutDialog : ConfiguracionIntent()
    object DismissLogoutDialog : ConfiguracionIntent()
    object Logout : ConfiguracionIntent()
}