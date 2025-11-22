package edu.ucne.farmaciacruz.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.data.local.PreferencesManager
import edu.ucne.farmaciacruz.domain.model.CarritoItem
import edu.ucne.farmaciacruz.domain.model.Producto
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.repository.CarritoRepository
import edu.ucne.farmaciacruz.domain.usecase.producto.GetProductosUseCase
import edu.ucne.farmaciacruz.domain.usecase.producto.SearchProductosUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductosViewModel @Inject constructor(
    private val getProductosUseCase: GetProductosUseCase,
    private val searchProductosUseCase: SearchProductosUseCase,
    private val carritoRepository: CarritoRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProductosState())
    val state: StateFlow<ProductosState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ProductosUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        onEvent(ProductosEvent.LoadProductos)
        loadCarrito()
    }

    fun onEvent(event: ProductosEvent) {
        when (event) {
            is ProductosEvent.LoadProductos -> handleLoadProductos()
            is ProductosEvent.SearchQueryChanged -> handleSearchQueryChanged(event.query)
            is ProductosEvent.CategoriaSelected -> handleCategoriaSelected(event.categoria)
            is ProductosEvent.ProductoClicked -> handleProductoClicked(event.productoId)
            is ProductosEvent.AddToCart -> handleAddToCart(event.producto)
            is ProductosEvent.RemoveFromCart -> handleRemoveFromCart(event.productoId)
            is ProductosEvent.UpdateQuantity -> handleUpdateQuantity(event.productoId, event.cantidad)
            is ProductosEvent.ClearError -> handleClearError()
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
                        _uiEvent.emit(ProductosUiEvent.ShowError(result.message ?: "Error desconocido"))
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
                        _uiEvent.emit(ProductosUiEvent.ShowError(result.message ?: "Error en la búsqueda"))
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
            _uiEvent.emit(ProductosUiEvent.NavigateToDetail(productoId))
        }
    }

    private fun handleAddToCart(producto: Producto) {
        val carritoActual = _state.value.carrito.toMutableList()
        val itemExistente = carritoActual.find { it.producto.id == producto.id }

        if (itemExistente != null) {
            itemExistente.cantidad++
            viewModelScope.launch {
                _uiEvent.emit(ProductosUiEvent.ShowSuccess("Cantidad actualizada en el carrito"))
            }
        } else {
            carritoActual.add(CarritoItem(producto, 1))
            viewModelScope.launch {
                _uiEvent.emit(ProductosUiEvent.ShowSuccess("Producto agregado al carrito"))
            }
        }

        _state.update { it.copy(carrito = carritoActual) }
    }

    private fun handleRemoveFromCart(productoId: Int) {
        val carritoActual = _state.value.carrito.toMutableList()
        carritoActual.removeAll { it.producto.id == productoId }
        _state.update { it.copy(carrito = carritoActual) }

        viewModelScope.launch {
            _uiEvent.emit(ProductosUiEvent.ShowSuccess("Producto eliminado del carrito"))
        }
    }

    private fun handleUpdateQuantity(productoId: Int, cantidad: Int) {
        val carritoActual = _state.value.carrito.toMutableList()
        val item = carritoActual.find { it.producto.id == productoId }

        if (item != null) {
            if (cantidad <= 0) {
                carritoActual.remove(item)
                viewModelScope.launch {
                    _uiEvent.emit(ProductosUiEvent.ShowSuccess("Producto eliminado del carrito"))
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

    private fun loadCarrito() {
        viewModelScope.launch {
            val usuarioId = preferencesManager.getUserId().first() ?: return@launch
            carritoRepository.getCarrito(usuarioId).collect { items ->
                _state.update { it.copy(carrito = items) }
            }
        }
    }
}