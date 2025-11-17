package edu.ucne.farmaciacruz.presentation.login.registro

sealed class RegistroIntent {
    data class NombreChanged(val nombre: String) : RegistroIntent()
    data class ApellidoChanged(val apellido: String) : RegistroIntent()
    data class EmailChanged(val email: String) : RegistroIntent()
    data class TelefonoChanged(val telefono: String) : RegistroIntent()
    data class PasswordChanged(val password: String) : RegistroIntent()
    data class ConfirmarPasswordChanged(val confirmarPassword: String) : RegistroIntent()
    data class TerminosChanged(val aceptado: Boolean) : RegistroIntent()
    object RegistrarClicked : RegistroIntent()
    object ClearError : RegistroIntent()
}