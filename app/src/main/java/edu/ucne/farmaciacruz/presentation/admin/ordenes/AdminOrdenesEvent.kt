package edu.ucne.farmaciacruz.presentation.admin.ordenes

import edu.ucne.farmaciacruz.domain.model.OrderAdmin
import edu.ucne.farmaciacruz.domain.model.OrderStatus

sealed class AdminOrdenesEvent {
    data object LoadOrdenes : AdminOrdenesEvent()
    data class SearchQueryChanged(val query: String) : AdminOrdenesEvent()
    data class EstadoFilterSelected(val estado: OrderStatus?) : AdminOrdenesEvent()
    data class OrdenSelected(val orden: OrderAdmin) : AdminOrdenesEvent()
    data object DismissDialogs : AdminOrdenesEvent()
    data class ShowChangeStatusDialog(val orden: OrderAdmin) : AdminOrdenesEvent()
    data class ChangeStatus(val orderId: Int, val nuevoEstado: String) : AdminOrdenesEvent()
    data class ShowDetalleDialog(val orden: OrderAdmin) : AdminOrdenesEvent()
    data object Refresh : AdminOrdenesEvent()
}