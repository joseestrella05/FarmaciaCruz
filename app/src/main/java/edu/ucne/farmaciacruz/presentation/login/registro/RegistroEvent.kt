package edu.ucne.farmaciacruz.presentation.login.registro

sealed class RegistroEvent {
    data class ShowError(val message: String) : RegistroEvent()
    data class ShowSuccess(val message: String) : RegistroEvent()
    object NavigateToHome : RegistroEvent()
}