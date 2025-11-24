package edu.ucne.farmaciacruz.data.remote.dto

data class OrderResponseDto(
    val orderId: Int,
    val usuarioId: Int,
    val total: Double,
    val estado: String,
    val productos: List<OrderProductDto>,
    val paypalOrderId: String?,
    val paypalPayerId: String?,
    val fechaCreacion: String,
    val fechaActualizacion: String
)