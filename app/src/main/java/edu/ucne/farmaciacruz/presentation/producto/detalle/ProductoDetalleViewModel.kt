package edu.ucne.farmaciacruz.presentation.producto.detalle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.usecase.carrito.AddToCarritoWithCantidadUseCase
import edu.ucne.farmaciacruz.domain.usecase.preference.GetUserIdUseCase
import edu.ucne.farmaciacruz.domain.usecase.producto.GetProductoPorIdUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProductoDetalleUiEvent {
    data class ShowSuccess(val message: String) : ProductoDetalleUiEvent()
    data class ShowError(val message: String) : ProductoDetalleUiEvent()
    object NavigateBack : ProductoDetalleUiEvent()
}

@HiltViewModel
class ProductoDetalleViewModel @Inject constructor(
    private val getProductoPorIdUseCase: GetProductoPorIdUseCase,
    private val addToCarritoWithCantidadUseCase: AddToCarritoWithCantidadUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ProductoDetalleState())
    val state = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ProductoDetalleUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val productoId: Int = checkNotNull(savedStateHandle["productoId"])

    init {
        loadProducto()
    }

    private fun loadProducto() {
        viewModelScope.launch {
            getProductoPorIdUseCase(productoId).collect { result ->
                when (result) {
                    is Resource.Loading ->
                        _state.update { it.copy(isLoading = true, error = null) }

                    is Resource.Success ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                producto = result.data,
                                error = null
                            )
                        }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                        _uiEvent.emit(
                            ProductoDetalleUiEvent.ShowError(
                                result.message ?: "Error desconocido"
                            )
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: ProductoDetalleEvent) {
        when (event) {
            is ProductoDetalleEvent.UpdateCantidad -> updateCantidad(event.cantidad)
            ProductoDetalleEvent.AddToCart -> addToCarrito()
            ProductoDetalleEvent.NavigateBack -> navigateBack()
        }
    }

    private fun updateCantidad(cantidad: Int) {
        if (cantidad > 0) {
            _state.update { it.copy(cantidad = cantidad) }
        }
    }

    private fun addToCarrito() = viewModelScope.launch {
        val producto = _state.value.producto ?: return@launch
        val cantidad = _state.value.cantidad

        val usuarioId = getUserIdUseCase().first() ?: run {
            _uiEvent.emit(ProductoDetalleUiEvent.ShowError("Usuario no identificado"))
            return@launch
        }

        try {
            addToCarritoWithCantidadUseCase(usuarioId, producto, cantidad)

            _uiEvent.emit(
                ProductoDetalleUiEvent.ShowSuccess(
                    "$cantidad ${producto.nombre} agregado(s) al carrito"
                )
            )

            _state.update { it.copy(cantidad = 1) }

        } catch (e: Exception) {
            _uiEvent.emit(ProductoDetalleUiEvent.ShowError("Error al agregar al carrito"))
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _uiEvent.emit(ProductoDetalleUiEvent.NavigateBack)
        }
    }
}
