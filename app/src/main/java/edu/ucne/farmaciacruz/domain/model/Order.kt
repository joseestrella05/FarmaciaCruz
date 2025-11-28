package edu.ucne.farmaciacruz.domain.model

data class Order(
    val orderId: Int,
    val usuarioId: Int,
    val total: Double,
    val estado: OrderStatus,
    val productos: List<OrderProduct>,
    val paypalOrderId: String?,
    val paypalPayerId: String?,
    val fechaCreacion: String,
    val fechaActualizacion: String
) {
    val totalFormateado: String
        get() = "$${String.format("%.2f", total)}"

    val cantidadProductos: Int
        get() = productos.sumOf { it.cantidad }
}

enum class OrderStatus {
    PENDIENTE,
    PROCESANDO,
    COMPLETADO,
    FALLIDO,
    CANCELADO
}