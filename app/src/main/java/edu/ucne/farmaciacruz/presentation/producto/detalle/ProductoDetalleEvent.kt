package edu.ucne.farmaciacruz.presentation.producto.detalle

sealed class ProductoDetalleEvent {
    object AddToCart : ProductoDetalleEvent()
    data class UpdateCantidad(val cantidad: Int) : ProductoDetalleEvent()
    object NavigateBack : ProductoDetalleEvent()
}