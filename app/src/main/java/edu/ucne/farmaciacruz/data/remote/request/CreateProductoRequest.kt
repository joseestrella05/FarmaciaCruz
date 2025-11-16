package edu.ucne.farmaciacruz.data.remote.request

data class CreateProductoRequest(
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val precio: Double,
    val imagenUrl: String
)