package edu.ucne.farmaciacruz.data.remote.dto

data class OrderProductDto(
    val productoId: Int,
    val nombre: String,
    val cantidad: Int,
    val precio: Double
)