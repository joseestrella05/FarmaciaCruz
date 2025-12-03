package edu.ucne.farmaciacruz.presentation.admin.dashboard

sealed class AdminDashboardEvent {
    data object LoadStats : AdminDashboardEvent()
    data class TabSelected(val tab: AdminTab) : AdminDashboardEvent()
    data object Refresh : AdminDashboardEvent()
}