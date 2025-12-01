package edu.ucne.farmaciacruz.presentation.admin.ordenes

import edu.ucne.farmaciacruz.domain.model.OrderAdmin
import edu.ucne.farmaciacruz.domain.model.OrderStatus

data class AdminOrdenesState(
    val isLoading: Boolean = false,
    val ordenes: List<OrderAdmin> = emptyList(),
    val ordenesFiltradas: List<OrderAdmin> = emptyList(),
    val searchQuery: String = "",
    val selectedEstado: OrderStatus? = null,
    val estados: List<OrderStatus> = OrderStatus.values().toList(),
    val ordenSeleccionada: OrderAdmin? = null,
    val showChangeStatusDialog: Boolean = false,
    val showDetalleDialog: Boolean = false,
    val error: String? = null
)