package edu.ucne.farmaciacruz.presentation.admin.productos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.domain.model.Producto
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.repository.ProductRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminProductosViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminProductosState())
    val state = _state.asStateFlow()
    private val _effect = Channel<AdminProductosUiEvent>()
    val effect = _effect.receiveAsFlow()

    init {
        loadProductos()
    }

    fun onEvent(event: AdminProductosEvent) {
        when (event) {
            AdminProductosEvent.LoadProductos -> loadProductos()
            is AdminProductosEvent.SearchQueryChanged -> search(event.query)
            is AdminProductosEvent.CategoriaSelected -> filterByCategoria(event.categoria)
            is AdminProductosEvent.ProductoSelected -> selectProducto(event.producto)
            AdminProductosEvent.ShowAddDialog -> showAddDialog()
            AdminProductosEvent.DismissDialogs -> dismissDialogs()
            is AdminProductosEvent.CreateProducto -> createProducto(
                event.nombre, event.categoria, event.descripcion,
                event.precio, event.imagenUrl
            )
            is AdminProductosEvent.UpdateProducto -> updateProducto(event.producto)
            is AdminProductosEvent.DeleteProducto -> showDeleteDialog(event.productoId)
            AdminProductosEvent.ConfirmDelete -> confirmDelete()
        }
    }

    private fun loadProductos() {
        viewModelScope.launch {
            productRepository.getProductos().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        val productos = result.data.orEmpty()
                        _state.update {
                            it.copy(
                                isLoading = false,
                                productos = productos,
                                productosFiltrados = productos,
                                categorias = productos.map { p -> p.categoria }.distinct().sorted(),
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                        _effect.send(
                            AdminProductosUiEvent.ShowError(
                                result.message ?: "Error al cargar productos"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun search(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    private fun filterByCategoria(categoria: String?) {
        _state.update { it.copy(selectedCategoria = categoria) }
        applyFilters()
    }

    private fun applyFilters() {
        val current = _state.value
        val filtered = current.productos.filter { producto ->
            val matchesSearch = current.searchQuery.isBlank() ||
                    producto.nombre.contains(current.searchQuery, ignoreCase = true) ||
                    producto.descripcion.contains(current.searchQuery, ignoreCase = true)

            val matchesCategoria = current.selectedCategoria == null ||
                    producto.categoria == current.selectedCategoria

            matchesSearch && matchesCategoria
        }

        _state.update { it.copy(productosFiltrados = filtered) }
    }


    private fun selectProducto(producto: Producto) {
        _state.update {
            it.copy(
                productoSeleccionado = producto,
                showEditDialog = true
            )
        }
    }

    private fun showAddDialog() {
        _state.update { it.copy(showAddDialog = true) }
    }

    private fun showDeleteDialog(productoId: Int) {
        val producto = _state.value.productos.find { it.id == productoId }
        _state.update {
            it.copy(
                productoSeleccionado = producto,
                showDeleteDialog = true
            )
        }
    }

    private fun dismissDialogs() {
        _state.update {
            it.copy(
                showAddDialog = false,
                showEditDialog = false,
                showDeleteDialog = false,
                productoSeleccionado = null
            )
        }
    }

    private fun createProducto(
        nombre: String,
        categoria: String,
        descripcion: String,
        precio: Double,
        imagenUrl: String
    ) {
        viewModelScope.launch {
            productRepository.createProducto(
                nombre, categoria, descripcion, precio, imagenUrl
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        _effect.send(
                            AdminProductosUiEvent.ShowSuccess("Producto creado exitosamente")
                        )
                        dismissDialogs()
                        loadProductos()
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false) }
                        _effect.send(
                            AdminProductosUiEvent.ShowError(
                                result.message ?: "Error al crear producto"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun updateProducto(producto: Producto) {
        viewModelScope.launch {
            productRepository.updateProducto(producto).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        _effect.send(
                            AdminProductosUiEvent.ShowSuccess("Producto actualizado exitosamente")
                        )
                        dismissDialogs()
                        loadProductos()
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false) }
                        _effect.send(
                            AdminProductosUiEvent.ShowError(
                                result.message ?: "Error al actualizar producto"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun confirmDelete() {
        val producto = _state.value.productoSeleccionado ?: return

        viewModelScope.launch {
            productRepository.deleteProducto(producto.id).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        _effect.send(
                            AdminProductosUiEvent.ShowSuccess("Producto eliminado exitosamente")
                        )
                        dismissDialogs()
                        loadProductos()
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false) }
                        _effect.send(
                            AdminProductosUiEvent.ShowError(
                                result.message ?: "Error al eliminar producto"
                            )
                        )
                    }
                }
            }
        }
    }
}