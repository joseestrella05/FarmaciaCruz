package edu.ucne.faemaciacruz.data.remote.dto

data class AuthResponseDto(
    val token: String,
    val refreshToken: String,
    val expiracion: String,
    val usuario: UsuarioDto
)