package edu.ucne.farmaciacruz.presentation.admin.ordenes

sealed class AdminOrdenesUiEvent {
    data class ShowError(val message: String) : AdminOrdenesUiEvent()
    data class ShowSuccess(val message: String) : AdminOrdenesUiEvent()
}