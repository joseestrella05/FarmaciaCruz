package edu.ucne.farmaciacruz.domain.model

data class OrderAdmin(
    val orderId: Int,
    val usuarioId: Int,
    val usuarioNombre: String,
    val total: Double,
    val estado: OrderStatus,
    val metodoPago: String,
    val cantidadProductos: Int,
    val fechaCreacion: String,
    val fechaActualizacion: String
) {
    val totalFormateado: String
        get() = "$${String.format("%.2f", total)}"
}