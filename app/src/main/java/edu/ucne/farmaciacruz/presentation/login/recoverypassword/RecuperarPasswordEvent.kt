package edu.ucne.farmaciacruz.presentation.login.recoverypassword

sealed class RecuperarPasswordEvent {
    data class EmailChanged(val email: String) : RecuperarPasswordEvent()
    data object EnviarClicked : RecuperarPasswordEvent()
    data object VolverLogin : RecuperarPasswordEvent()
    data object ClearError : RecuperarPasswordEvent()
}

