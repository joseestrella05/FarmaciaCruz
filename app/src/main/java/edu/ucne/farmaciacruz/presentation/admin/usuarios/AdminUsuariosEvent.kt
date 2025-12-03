package edu.ucne.farmaciacruz.presentation.admin.usuarios

import edu.ucne.farmaciacruz.domain.model.UsuarioAdmin

sealed class AdminUsuariosEvent {
    data object LoadUsuarios : AdminUsuariosUiEvent()
    data class SearchQueryChanged(val query: String) : AdminUsuariosUiEvent()
    data class RolFilterSelected(val rol: String?) : AdminUsuariosUiEvent()
    data class EstadoFilterSelected(val activo: Boolean?) : AdminUsuariosUiEvent()
    data class UsuarioSelected(val usuario: UsuarioAdmin) : AdminUsuariosUiEvent()
    data object DismissDialogs : AdminUsuariosUiEvent()
    data class ShowCambiarRolDialog(val usuario: UsuarioAdmin) : AdminUsuariosUiEvent()
    data class ShowToggleEstadoDialog(val usuario: UsuarioAdmin) : AdminUsuariosUiEvent()
    data class ShowDeleteDialog(val usuario: UsuarioAdmin) : AdminUsuariosUiEvent()
    data class CambiarRol(val usuarioId: Int, val nuevoRol: String) : AdminUsuariosUiEvent()
    data class ToggleEstado(val usuarioId: Int, val activo: Boolean) : AdminUsuariosUiEvent()
    data object ConfirmDelete : AdminUsuariosUiEvent()
    data object Refresh : AdminUsuariosUiEvent()

}