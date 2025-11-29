package edu.ucne.farmaciacruz.presentation.admin.dashboard

sealed class AdminDashboardEffect {
    data class ShowError(val message: String) : AdminDashboardEffect()
    data class ShowSuccess(val message: String) : AdminDashboardEffect()
    data object NavigateToProductos : AdminDashboardEffect()
    data object NavigateToUsuarios : AdminDashboardEffect()
    data object NavigateToOrdenes : AdminDashboardEffect()
}
