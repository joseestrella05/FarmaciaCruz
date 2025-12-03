package edu.ucne.farmaciacruz.presentation.admin.productos

sealed class AdminProductosUiEvent {
    data class ShowError(val message: String) : AdminProductosUiEvent()
    data class ShowSuccess(val message: String) : AdminProductosUiEvent()
}