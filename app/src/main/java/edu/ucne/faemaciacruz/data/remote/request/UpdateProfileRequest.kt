package edu.ucne.faemaciacruz.data.remote.request

data class UpdateProfileRequest(
    val nombre: String,
    val apellido: String,
    val telefono: String?
)