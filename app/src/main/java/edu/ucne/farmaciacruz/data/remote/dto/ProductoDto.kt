package edu.ucne.farmaciacruz.data.remote.dto

data class ProductoDto(
    val productoId: Int,
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val precio: Double,
    val imagenUrl: String
)