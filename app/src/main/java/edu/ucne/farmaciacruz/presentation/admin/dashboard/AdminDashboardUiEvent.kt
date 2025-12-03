package edu.ucne.farmaciacruz.presentation.admin.dashboard

sealed class AdminDashboardUiEvent {
    data class ShowError(val message: String) : AdminDashboardUiEvent()
    data class ShowSuccess(val message: String) : AdminDashboardUiEvent()
    data object NavigateToProductos : AdminDashboardUiEvent()
    data object NavigateToUsuarios : AdminDashboardUiEvent()
    data object NavigateToOrdenes : AdminDashboardUiEvent()
}
