package edu.ucne.farmaciacruz.presentation.admin.dashboard

import edu.ucne.farmaciacruz.domain.model.AdminStats

data class AdminDashboardState(
    val isLoading: Boolean = false,
    val stats: AdminStats? = null,
    val error: String? = null,
    val selectedTab: AdminTab = AdminTab.DASHBOARD
)