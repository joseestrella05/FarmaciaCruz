package edu.ucne.farmaciacruz.presentation.admin.productos

import edu.ucne.farmaciacruz.domain.model.Producto

data class AdminProductosState(
    val isLoading: Boolean = false,
    val productos: List<Producto> = emptyList(),
    val productosFiltrados: List<Producto> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoria: String? = null,
    val categorias: List<String> = emptyList(),
    val productoSeleccionado: Producto? = null,
    val showDeleteDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showAddDialog: Boolean = false,
    val error: String? = null
)