package edu.ucne.farmaciacruz.domain.model

data class OrderProduct(
    val productoId: Int,
    val nombre: String,
    val cantidad: Int,
    val precio: Double
) {
    val subtotal: Double
        get() = precio * cantidad

    val subtotalFormateado: String
        get() = "$${String.format("%.2f", subtotal)}"
}