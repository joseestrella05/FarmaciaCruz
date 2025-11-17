package edu.ucne.farmaciacruz.presentation.Configuracion

sealed class ConfiguracionEvent {
    data class ShowError(val message: String) : ConfiguracionEvent()
    data class ShowSuccess(val message: String) : ConfiguracionEvent()
    object NavigateToLogin : ConfiguracionEvent()
}