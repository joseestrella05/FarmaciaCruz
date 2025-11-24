package edu.ucne.farmaciacruz.domain.model

data class PaymentOrder(
    val id: String = "",
    val usuarioId: Int,
    val total: Double,
    val productos: List<CarritoItem>,
    val estado: PaymentStatus = PaymentStatus.PENDIENTE,
    val metodoPago: String = "PayPal",
    val paypalOrderId: String? = null,
    val paypalPayerId: String? = null,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis(),
    val sincronizado: Boolean = false,
    val errorMessage: String? = null
) {
    val totalFormateado: String
        get() = "$${String.format("%.2f", total)}"

    val cantidadProductos: Int
        get() = productos.sumOf { it.cantidad }
}

enum class PaymentStatus {
    PENDIENTE,
    PROCESANDO,
    COMPLETADO,
    FALLIDO,
    CANCELADO,
    REEMBOLSADO
}
