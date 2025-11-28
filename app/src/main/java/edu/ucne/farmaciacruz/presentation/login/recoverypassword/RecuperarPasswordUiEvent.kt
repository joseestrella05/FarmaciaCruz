package edu.ucne.farmaciacruz.presentation.login.recoverypassword

sealed class RecuperarPasswordUiEvent {
    data class ShowError(val message: String) : RecuperarPasswordUiEvent()
    data class ShowSuccess(val message: String) : RecuperarPasswordUiEvent()
    data object NavigateToLogin : RecuperarPasswordUiEvent()
}