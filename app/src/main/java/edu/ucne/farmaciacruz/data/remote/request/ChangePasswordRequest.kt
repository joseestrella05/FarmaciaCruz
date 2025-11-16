package edu.ucne.farmaciacruz.data.remote.request

data class ChangePasswordRequest(
    val passwordActual: String,
    val passwordNuevo: String
)