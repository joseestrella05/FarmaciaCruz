package edu.ucne.faemaciacruz.data.remote.request

data class ChangePasswordRequest(
    val passwordActual: String,
    val passwordNuevo: String
)