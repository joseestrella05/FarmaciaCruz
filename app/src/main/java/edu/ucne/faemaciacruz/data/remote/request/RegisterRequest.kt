package edu.ucne.faemaciacruz.data.remote.request

data class RegisterRequest(
    val email: String,
    val password: String,
    val nombre: String,
    val apellido: String,
    val telefono: String?
)