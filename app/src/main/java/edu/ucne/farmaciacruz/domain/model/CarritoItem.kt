package edu.ucne.farmaciacruz.domain.model

data class CarritoItem(
    val producto: Producto,
    val cantidad: Int = 1
) {
    val subtotal: Double
        get() = producto.precio * cantidad

    val subtotalFormateado: String
        get() = "$${String.format("%.2f", subtotal)}"
}