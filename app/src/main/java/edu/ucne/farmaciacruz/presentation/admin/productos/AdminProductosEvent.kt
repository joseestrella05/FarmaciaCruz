package edu.ucne.farmaciacruz.presentation.admin.productos

import edu.ucne.farmaciacruz.domain.model.Producto

sealed class AdminProductosEvent {
    data object LoadProductos : AdminProductosEvent()
    data class SearchQueryChanged(val query: String) : AdminProductosEvent()
    data class CategoriaSelected(val categoria: String?) : AdminProductosEvent()
    data class ProductoSelected(val producto: Producto) : AdminProductosEvent()
    data object ShowAddDialog : AdminProductosEvent()
    data object DismissDialogs : AdminProductosEvent()
    data class CreateProducto(
        val nombre: String,
        val categoria: String,
        val descripcion: String,
        val precio: Double,
        val imagenUrl: String
    ) : AdminProductosEvent()
    data class UpdateProducto(val producto: Producto) : AdminProductosEvent()
    data class DeleteProducto(val productoId: Int) : AdminProductosEvent()
    data object ConfirmDelete : AdminProductosEvent()
}