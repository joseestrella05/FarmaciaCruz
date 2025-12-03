package edu.ucne.farmaciacruz.presentation.admin.ordenes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.domain.model.OrderAdmin
import edu.ucne.farmaciacruz.domain.model.OrderStatus
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.usecase.admin.GetAllOrdersUseCase
import edu.ucne.farmaciacruz.domain.usecase.admin.UpdateOrderStatusUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminOrdenesViewModel @Inject constructor(
    private val getAllOrdersUseCase: GetAllOrdersUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminOrdenesState())
    val state = _state.asStateFlow()

    private val _uiEvent = Channel<AdminOrdenesUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadOrdenes()
    }

    fun onEvent(event: AdminOrdenesEvent) {
        when (event) {
            AdminOrdenesEvent.LoadOrdenes -> loadOrdenes()
            is AdminOrdenesEvent.SearchQueryChanged -> search(event.query)
            is AdminOrdenesEvent.EstadoFilterSelected -> filterByEstado(event.estado)
            is AdminOrdenesEvent.OrdenSelected -> selectOrden(event.orden)
            AdminOrdenesEvent.DismissDialogs -> dismissDialogs()
            is AdminOrdenesEvent.ShowChangeStatusDialog -> showChangeStatusDialog(event.orden)
            is AdminOrdenesEvent.ChangeStatus -> changeStatus(event.orderId, event.nuevoEstado)
            is AdminOrdenesEvent.ShowDetalleDialog -> showDetalleDialog(event.orden)
            AdminOrdenesEvent.Refresh -> loadOrdenes()
        }
    }

    private fun emitUiEvent(event: AdminOrdenesUiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }

    private fun loadOrdenes() {
        viewModelScope.launch {
            getAllOrdersUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        val ordenes = result.data ?: emptyList()
                        _state.update {
                            it.copy(
                                isLoading = false,
                                ordenes = ordenes,
                                ordenesFiltradas = ordenes,
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
                        emitUiEvent(
                            AdminOrdenesUiEvent.ShowError(
                                result.message ?: "Error al cargar órdenes"
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

    private fun filterByEstado(estado: OrderStatus?) {
        _state.update { it.copy(selectedEstado = estado) }
        applyFilters()
    }

    private fun applyFilters() {
        val current = _state.value
        val filtered = current.ordenes.filter { orden ->
            val matchesSearch = current.searchQuery.isBlank() ||
                    orden.orderId.toString().contains(current.searchQuery) ||
                    orden.usuarioNombre.contains(current.searchQuery, ignoreCase = true)

            val matchesEstado = current.selectedEstado == null ||
                    orden.estado == current.selectedEstado

            matchesSearch && matchesEstado
        }

        _state.update { it.copy(ordenesFiltradas = filtered) }
    }

    private fun selectOrden(orden: OrderAdmin) {
        _state.update { it.copy(ordenSeleccionada = orden) }
    }

    private fun showChangeStatusDialog(orden: OrderAdmin) {
        _state.update {
            it.copy(
                ordenSeleccionada = orden,
                showChangeStatusDialog = true
            )
        }
    }

    private fun showDetalleDialog(orden: OrderAdmin) {
        _state.update {
            it.copy(
                ordenSeleccionada = orden,
                showDetalleDialog = true
            )
        }
    }

    private fun dismissDialogs() {
        _state.update {
            it.copy(
                showChangeStatusDialog = false,
                showDetalleDialog = false,
                ordenSeleccionada = null
            )
        }
    }

    private fun changeStatus(orderId: Int, nuevoEstado: String) {
        viewModelScope.launch {
            updateOrderStatusUseCase(orderId, nuevoEstado).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        emitUiEvent(
                            AdminOrdenesUiEvent.ShowSuccess("Estado actualizado exitosamente")
                        )
                        dismissDialogs()
                        loadOrdenes()
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false) }
                        emitUiEvent(
                            AdminOrdenesUiEvent.ShowError(
                                result.message ?: "Error al actualizar estado"
                            )
                        )
                    }
                }
            }
        }
    }
}