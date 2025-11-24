package edu.ucne.farmaciacruz.presentation.checkout

import edu.ucne.farmaciacruz.domain.model.CarritoItem

data class CheckoutState(
    val carrito: List<CarritoItem> = emptyList(),
    val total: Double = 0.0,
    val isLoading: Boolean = false,
    val paypalOrderId: String? = null,
    val error: String? = null,
    val paymentCompleted: Boolean = false
) {
    val totalFormateado: String
        get() = "$${String.format("%.2f", total)}"

    val cantidadProductos: Int
        get() = carrito.sumOf { it.cantidad }
}