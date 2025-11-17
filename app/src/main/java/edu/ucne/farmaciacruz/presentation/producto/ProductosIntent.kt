package edu.ucne.farmaciacruz.presentation.producto

import edu.ucne.farmaciacruz.domain.model.Producto

sealed class ProductosIntent {
    object LoadProductos : ProductosIntent()
    data class SearchQueryChanged(val query: String) : ProductosIntent()
    data class CategoriaSelected(val categoria: String?) : ProductosIntent()
    data class ProductoClicked(val productoId: Int) : ProductosIntent()
    data class AddToCart(val producto: Producto) : ProductosIntent()
    data class RemoveFromCart(val productoId: Int) : ProductosIntent()
    data class UpdateQuantity(val productoId: Int, val cantidad: Int) : ProductosIntent()
    object ClearError : ProductosIntent()
}