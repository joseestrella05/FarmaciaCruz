package edu.ucne.farmaciacruz.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.domain.model.CarritoItem
import edu.ucne.farmaciacruz.domain.model.Producto
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.usecase.producto.GetProductosUseCase
import edu.ucne.farmaciacruz.domain.usecase.producto.SearchProductosUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductosViewModel @Inject constructor(
    private val getProductosUseCase: GetProductosUseCase,
    private val searchProductosUseCase: SearchProductosUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProductosState())
    val state: StateFlow<ProductosState> = _state.asStateFlow()

    private val _event = Channel<ProductosEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        processIntent(ProductosIntent.LoadProductos)
    }

    fun processIntent(intent: ProductosIntent) {
        when (intent) {
            is ProductosIntent.LoadProductos -> handleLoadProductos()
            is ProductosIntent.SearchQueryChanged -> handleSearchQueryChanged(intent.query)
            is ProductosIntent.CategoriaSelected -> handleCategoriaSelected(intent.categoria)
            is ProductosIntent.ProductoClicked -> handleProductoClicked(intent.productoId)
            is ProductosIntent.AddToCart -> handleAddToCart(intent.producto)
            is ProductosIntent.RemoveFromCart -> handleRemoveFromCart(intent.productoId)
            is ProductosIntent.UpdateQuantity -> handleUpdateQuantity(intent.productoId, intent.cantidad)
            is ProductosIntent.ClearError -> handleClearError()
        }
    }

    private fun handleLoadProductos() {
        viewModelScope.launch {
            getProductosUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        val productos = result.data ?: emptyList()
                        val categorias = productos.map { it.categoria }.distinct().sorted()

                        _state.update {
                            it.copy(
                                isLoading = false,
                                productos = productos,
                                productosFiltrados = productos,
                                categorias = categorias,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        _event.send(ProductosEvent.ShowError(result.message ?: "Error desconocido"))
                    }
                }
            }
        }
    }

    private fun handleSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }

        if (query.isBlank()) {
            val productosFiltrados = if (_state.value.selectedCategoria != null) {
                _state.value.productos.filter { it.categoria == _state.value.selectedCategoria }
            } else {
                _state.value.productos
            }
            _state.update { it.copy(productosFiltrados = productosFiltrados) }
        } else {
            searchProductos(query)
        }
    }

    private fun searchProductos(query: String) {
        viewModelScope.launch {
            searchProductosUseCase(query).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        val productos = result.data ?: emptyList()

                        // Aplicar filtro de categoría si está seleccionada
                        val productosFiltrados = if (_state.value.selectedCategoria != null) {
                            productos.filter { it.categoria == _state.value.selectedCategoria }
                        } else {
                            productos
                        }

                        _state.update {
                            it.copy(
                                productosFiltrados = productosFiltrados,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = result.message,
                                isLoading = false
                            )
                        }
                        _event.send(ProductosEvent.ShowError(result.message ?: "Error en la búsqueda"))
                    }
                }
            }
        }
    }

    private fun handleCategoriaSelected(categoria: String?) {
        _state.update { it.copy(selectedCategoria = categoria) }
        val productosFiltrados = if (categoria != null) {
            if (_state.value.searchQuery.isBlank()) {
                _state.value.productos.filter { it.categoria == categoria }
            } else {
                _state.value.productosFiltrados.filter { it.categoria == categoria }
            }
        } else {
            if (_state.value.searchQuery.isBlank()) {
                _state.value.productos
            } else {
                _state.value.productosFiltrados
            }
        }

        _state.update { it.copy(productosFiltrados = productosFiltrados) }
    }

    private fun handleProductoClicked(productoId: Int) {
        viewModelScope.launch {
            _event.send(ProductosEvent.NavigateToDetail(productoId))
        }
    }

    private fun handleAddToCart(producto: Producto) {
        val carritoActual = _state.value.carrito.toMutableList()
        val itemExistente = carritoActual.find { it.producto.id == producto.id }

        if (itemExistente != null) {
            itemExistente.cantidad++
            viewModelScope.launch {
                _event.send(ProductosEvent.ShowSuccess("Cantidad actualizada en el carrito"))
            }
        } else {
            carritoActual.add(CarritoItem(producto, 1))
            viewModelScope.launch {
                _event.send(ProductosEvent.ShowSuccess("Producto agregado al carrito"))
            }
        }

        _state.update { it.copy(carrito = carritoActual) }
    }

    private fun handleRemoveFromCart(productoId: Int) {
        val carritoActual = _state.value.carrito.toMutableList()
        carritoActual.removeAll { it.producto.id == productoId }
        _state.update { it.copy(carrito = carritoActual) }

        viewModelScope.launch {
            _event.send(ProductosEvent.ShowSuccess("Producto eliminado del carrito"))
        }
    }

    private fun handleUpdateQuantity(productoId: Int, cantidad: Int) {
        val carritoActual = _state.value.carrito.toMutableList()
        val item = carritoActual.find { it.producto.id == productoId }

        if (item != null) {
            if (cantidad <= 0) {
                carritoActual.remove(item)
                viewModelScope.launch {
                    _event.send(ProductosEvent.ShowSuccess("Producto eliminado del carrito"))
                }
            } else {
                item.cantidad = cantidad
            }
        }

        _state.update { it.copy(carrito = carritoActual) }
    }

    private fun handleClearError() {
        _state.update { it.copy(error = null) }
    }

    fun getTotalCarrito(): Double {
        return _state.value.carrito.sumOf { it.subtotal }
    }

    fun getCantidadItemsCarrito(): Int {
        return _state.value.carrito.sumOf { it.cantidad }
    }
}