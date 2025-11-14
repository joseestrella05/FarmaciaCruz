package edu.ucne.faemaciacruz.presentation.producto

import edu.ucne.faemaciacruz.domain.model.CarritoItem
import edu.ucne.faemaciacruz.domain.model.Producto

data class ProductosState(
    val isLoading: Boolean = false,
    val productos: List<Producto> = emptyList(),
    val productosFiltrados: List<Producto> = emptyList(),
    val categorias: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoria: String? = null,
    val carrito: List<CarritoItem> = emptyList(),
    val error: String? = null
)