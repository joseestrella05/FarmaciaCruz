package edu.ucne.farmaciacruz.presentation.producto.detalle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.data.local.PreferencesManager
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.repository.CarritoRepository
import edu.ucne.farmaciacruz.domain.usecase.producto.GetProductoPorIdUseCase
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
class ProductoDetalleViewModel @Inject constructor(
    private val getProductoPorIdUseCase: GetProductoPorIdUseCase,
    private val carritoRepository: CarritoRepository,
    private val preferencesManager: PreferencesManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ProductoDetalleState())
    val state: StateFlow<ProductoDetalleState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ProductoDetalleUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val productoId: Int = checkNotNull(savedStateHandle["productoId"])

    init {
        onEvent(ProductoDetalleEvent.LoadProducto(productoId))
    }

    fun onEvent(event: ProductoDetalleEvent) {
        when (event) {
            is ProductoDetalleEvent.LoadProducto -> handleLoadProducto(event.productoId)
            is ProductoDetalleEvent.AddToCart -> handleAddToCart()
            is ProductoDetalleEvent.UpdateQuantity -> handleUpdateQuantity(event.cantidad)
            is ProductoDetalleEvent.NavigateBack -> handleNavigateBack()
        }
    }

    private fun handleLoadProducto(productoId: Int) {
        viewModelScope.launch {
            getProductoPorIdUseCase(productoId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                producto = result.data,
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
                        _uiEvent.emit(ProductoDetalleUiEvent.ShowError(result.message ?: "Error desconocido"))
                    }
                }
            }
        }
    }

    private fun handleAddToCart() {
        val producto = _state.value.producto ?: return

        viewModelScope.launch {
            try {
                val usuarioId = preferencesManager.getUserId().first() ?: return@launch

                repeat(_state.value.cantidad) {
                    carritoRepository.addToCarrito(usuarioId, producto)
                }

                _uiEvent.emit(
                    ProductoDetalleUiEvent.ShowSuccess(
                        "${_state.value.cantidad} ${producto.nombre} agregado(s) al carrito"
                    )
                )

                _state.update { it.copy(cantidad = 1) }
            } catch (e: Exception) {
                _uiEvent.emit(ProductoDetalleUiEvent.ShowError("Error al agregar al carrito"))
            }
        }
    }

    private fun handleUpdateQuantity(cantidad: Int) {
        if (cantidad > 0) {
            _state.update { it.copy(cantidad = cantidad) }
        }
    }

    private fun handleNavigateBack() {
        viewModelScope.launch {
            _uiEvent.emit(ProductoDetalleUiEvent.NavigateBack)
        }
    }
}