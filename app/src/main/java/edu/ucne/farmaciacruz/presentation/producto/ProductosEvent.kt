package edu.ucne.farmaciacruz.presentation.producto

sealed class ProductosEvent {
    data class ShowError(val message: String) : ProductosEvent()
    data class ShowSuccess(val message: String) : ProductosEvent()
    data class NavigateToDetail(val productoId: Int) : ProductosEvent()
}