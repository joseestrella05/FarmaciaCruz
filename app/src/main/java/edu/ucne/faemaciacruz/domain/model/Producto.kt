package edu.ucne.faemaciacruz.domain.model

data class Producto(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val precio: Double,
    val imagenUrl: String
) {
    val precioFormateado: String
        get() = "$${String.format("%.2f", precio)}"
}