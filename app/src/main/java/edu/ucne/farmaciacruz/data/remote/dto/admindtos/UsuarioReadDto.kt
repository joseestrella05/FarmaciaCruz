package edu.ucne.farmaciacruz.data.remote.dto.admindtos

data class UsuarioReadDto(
    val usuarioId: Int,
    val email: String,
    val nombre: String,
    val apellido: String,
    val telefono: String?,
    val rol: String,
    val activo: Boolean,
    val emailConfirmado: Boolean,
    val fechaCreacion: String
)