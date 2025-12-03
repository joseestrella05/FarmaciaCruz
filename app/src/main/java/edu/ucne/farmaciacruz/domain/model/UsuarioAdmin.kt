package edu.ucne.farmaciacruz.domain.model

data class UsuarioAdmin(
    val usuarioId: Int,
    val email: String,
    val nombre: String,
    val apellido: String,
    val telefono: String?,
    val rol: String,
    val activo: Boolean,
    val emailConfirmado: Boolean,
    val fechaCreacion: String,
    val ultimoAcceso: String?
) {
    val nombreCompleto: String
        get() = "$nombre $apellido"
}