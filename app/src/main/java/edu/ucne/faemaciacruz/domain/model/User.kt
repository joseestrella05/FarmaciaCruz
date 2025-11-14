package edu.ucne.faemaciacruz.domain.model

data class User(
    val id: Int,
    val email: String,
    val nombre: String,
    val apellido: String,
    val telefono: String?,
    val rol: String
) {
    val nombreCompleto: String
        get() = "$nombre $apellido"
}
