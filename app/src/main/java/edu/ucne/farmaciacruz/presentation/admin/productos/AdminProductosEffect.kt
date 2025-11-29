package edu.ucne.farmaciacruz.presentation.admin.productos

sealed class AdminProductosEffect {
    data class ShowError(val message: String) : AdminProductosEffect()
    data class ShowSuccess(val message: String) : AdminProductosEffect()
}