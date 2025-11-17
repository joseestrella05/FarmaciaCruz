package edu.ucne.farmaciacruz.presentation.login.registro

import edu.ucne.farmaciacruz.domain.model.User

data class RegistroState(
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val telefono: String = "",
    val password: String = "",
    val confirmarPassword: String = "",
    val aceptaTerminos: Boolean = false,
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)