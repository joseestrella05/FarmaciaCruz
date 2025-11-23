package edu.ucne.farmaciacruz.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.domain.model.Producto
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.usecase.carrito.AddToCarritoUseCase
import edu.ucne.farmaciacruz.domain.usecase.carrito.GetCarritoUseCase
import edu.ucne.farmaciacruz.domain.usecase.carrito.RemoveFromCarritoUseCase
import edu.ucne.farmaciacruz.domain.usecase.carrito.UpdateCarritoCantidadUseCase
import edu.ucne.farmaciacruz.domain.usecase.preference.GetUserIdUseCase
import edu.ucne.farmaciacruz.domain.usecase.producto.GetProductosUseCase
import edu.ucne.farmaciacruz.domain.usecase.producto.SearchProductosUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val getCarritoUseCase: GetCarritoUseCase,
    private val addToCarritoUseCase: AddToCarritoUseCase,
    private val removeFromCarritoUseCase: RemoveFromCarritoUseCase,
    private val updateCantidadUseCase: UpdateCarritoCantidadUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProductosState())
    val state = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ProductosUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        onEvent(ProductosEvent.LoadProductos)
        observarCarrito()
    }

    fun onEvent(event: ProductosEvent) {
        when (event) {
            ProductosEvent.LoadProductos -> loadProductos()
            is ProductosEvent.SearchQueryChanged -> search(event.query)
            is ProductosEvent.CategoriaSelected -> filtrarCategoria(event.categoria)
            is ProductosEvent.ProductoClicked ->
                viewModelScope.launch { _uiEvent.emit(ProductosUiEvent.NavigateToDetail(event.productoId)) }

            is ProductosEvent.AddToCart -> add(event.producto)
            is ProductosEvent.RemoveFromCart -> remove(event.productoId)
            is ProductosEvent.UpdateQuantity -> updateQty(event.productoId, event.cantidad)
            ProductosEvent.ClearError -> _state.update { it.copy(error = null) }
        }
    }

    private fun observarCarrito() = viewModelScope.launch {
        val usuarioId = getUserIdUseCase().first() ?: return@launch
        getCarritoUseCase(usuarioId).collect { items ->
            _state.update { it.copy(carrito = items) }
        }
    }

    private fun add(producto: Producto) = viewModelScope.launch {
        val usuarioId = getUserIdUseCase().first() ?: return@launch
        addToCarritoUseCase(usuarioId, producto)
        _uiEvent.emit(ProductosUiEvent.ShowSuccess("Agregado al carrito"))
    }

    private fun remove(productoId: Int) = viewModelScope.launch {
        val usuarioId = getUserIdUseCase().first() ?: return@launch
        removeFromCarritoUseCase(usuarioId, productoId)
        _uiEvent.emit(ProductosUiEvent.ShowSuccess("Eliminado del carrito"))
    }

    private fun updateQty(productoId: Int, cantidad: Int) = viewModelScope.launch {
        val usuarioId = getUserIdUseCase().first() ?: return@launch
        updateCantidadUseCase(usuarioId, productoId, cantidad)
    }

    private fun loadProductos() = viewModelScope.launch {
        getProductosUseCase().collect { result ->
            when (result) {
                is Resource.Loading -> _state.update { it.copy(isLoading = true, error = null) }
                is Resource.Success -> {
                    val productos = result.data.orEmpty()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            productos = productos,
                            productosFiltrados = productos,
                            categorias = productos.map { it.categoria }.distinct().sorted()
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                    _uiEvent.emit(ProductosUiEvent.ShowError(result.message ?: "Error"))
                }
            }
        }
    }

    private fun search(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            filtrarCategoria(_state.value.selectedCategoria)
        } else {
            viewModelScope.launch {
                searchProductosUseCase(query).collect { r ->
                    if (r is Resource.Success) {
                        val list = r.data.orEmpty()
                        filtrarConLista(list)
                    }
                    if (r is Resource.Error) {
                        _state.update { it.copy(error = r.message, isLoading = false) }
                    }
                }
            }
        }
    }

    private fun filtrarCategoria(cat: String?) {
        _state.update { it.copy(selectedCategoria = cat) }
        filtrarConLista(_state.value.productos)
    }

    private fun filtrarConLista(base: List<Producto>) {
        val cat = _state.value.selectedCategoria
        val q = _state.value.searchQuery
        val filtered = base.filter {
            (cat == null || it.categoria == cat) &&
                    (q.isBlank() || it.nombre.contains(q, true))
        }
        _state.update { it.copy(productosFiltrados = filtered, isLoading = false) }
    }
}