package edu.ucne.farmaciacruz.presentation.admin.usuarios

sealed class AdminUsuariosUiEvent {
    data class ShowError(val message: String) : AdminUsuariosUiEvent()
    data class ShowSuccess(val message: String) : AdminUsuariosUiEvent()
}