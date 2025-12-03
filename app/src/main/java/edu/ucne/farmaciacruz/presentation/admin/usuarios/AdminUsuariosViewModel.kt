package edu.ucne.farmaciacruz.presentation.admin.usuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.usecase.admin.CambiarEstadoUsuarioUseCase
import edu.ucne.farmaciacruz.domain.usecase.admin.CambiarRolUsuarioUseCase
import edu.ucne.farmaciacruz.domain.usecase.admin.DeleteUsuarioUseCase
import edu.ucne.farmaciacruz.domain.usecase.admin.GetAllUsuariosUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminUsuariosViewModel @Inject constructor(
    private val getAllUsuariosUseCase: GetAllUsuariosUseCase,
    private val cambiarRolUsuarioUseCase: CambiarRolUsuarioUseCase,
    private val cambiarEstadoUsuarioUseCase: CambiarEstadoUsuarioUseCase,
    private val deleteUsuarioUseCase: DeleteUsuarioUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUsuariosState())
    val state = _state.asStateFlow()

    private val _uiEvent = Channel<AdminUsuariosUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        onEvent(AdminUsuariosEvent.LoadUsuarios)
    }

    fun onEvent(event: AdminUsuariosEvent) {
        when (event) {

            AdminUsuariosEvent.LoadUsuarios ->
                loadUsuarios()

            is AdminUsuariosEvent.SearchQueryChanged ->
                reduce { it.copy(searchQuery = event.query) }
                    .also { applyFilters() }

            is AdminUsuariosEvent.RolFilterSelected ->
                reduce { it.copy(selectedRol = event.rol) }
                    .also { applyFilters() }

            is AdminUsuariosEvent.EstadoFilterSelected ->
                reduce { it.copy(selectedEstado = event.activo) }
                    .also { applyFilters() }

            is AdminUsuariosEvent.UsuarioSelected ->
                reduce { it.copy(usuarioSeleccionado = event.usuario) }

            AdminUsuariosEvent.DismissDialogs ->
                reduce {
                    it.copy(
                        showEditRolDialog = false,
                        showToggleEstadoDialog = false,
                        showDeleteDialog = false,
                        usuarioSeleccionado = null
                    )
                }

            is AdminUsuariosEvent.ShowCambiarRolDialog ->
                reduce {
                    it.copy(
                        usuarioSeleccionado = event.usuario,
                        showEditRolDialog = true
                    )
                }

            is AdminUsuariosEvent.ShowToggleEstadoDialog ->
                reduce {
                    it.copy(
                        usuarioSeleccionado = event.usuario,
                        showToggleEstadoDialog = true
                    )
                }

            is AdminUsuariosEvent.ShowDeleteDialog ->
                reduce {
                    it.copy(
                        usuarioSeleccionado = event.usuario,
                        showDeleteDialog = true
                    )
                }

            is AdminUsuariosEvent.CambiarRol ->
                cambiarRol(event.usuarioId, event.nuevoRol)

            is AdminUsuariosEvent.ToggleEstado ->
                toggleEstado(event.usuarioId, event.activo)

            AdminUsuariosEvent.ConfirmDelete ->
                confirmDelete()

            AdminUsuariosEvent.Refresh ->
                loadUsuarios()
        }
    }

    private fun reduce(block: (AdminUsuariosState) -> AdminUsuariosState) {
        _state.update(block)
    }

    private suspend fun sendUiEvent(event: AdminUsuariosUiEvent) {
        _uiEvent.send(event)
    }

    private fun loadUsuarios() {
        viewModelScope.launch {
            getAllUsuariosUseCase().collect { result ->
                when (result) {

                    is Resource.Loading ->
                        reduce { it.copy(isLoading = true) }

                    is Resource.Success -> {
                        reduce {
                            it.copy(
                                isLoading = false,
                                usuarios = result.data.orEmpty()
                            )
                        }
                        applyFilters()
                    }

                    is Resource.Error -> {
                        reduce { it.copy(isLoading = false) }
                        sendUiEvent(AdminUsuariosUiEvent.ShowError(result.message ?: "Error"))
                    }
                }
            }
        }
    }

    private fun applyFilters() {
        val s = _state.value

        val filtered = s.usuarios.filter { usuario ->

            val matchesSearch =
                s.searchQuery.isBlank() ||
                        usuario.nombreCompleto.contains(s.searchQuery, true) ||
                        usuario.email.contains(s.searchQuery, true)

            val matchesRol =
                s.selectedRol == null || usuario.rol == s.selectedRol

            val matchesEstado =
                s.selectedEstado == null || usuario.activo == s.selectedEstado

            matchesSearch && matchesRol && matchesEstado
        }

        reduce { it.copy(usuariosFiltrados = filtered) }
    }

    private suspend fun handleMutation(
        result: Resource<Unit>,
        successMsg: String,
        errorMsg: String
    ) {
        when (result) {

            is Resource.Loading ->
                reduce { it.copy(isLoading = true) }

            is Resource.Success -> {
                reduce { it.copy(isLoading = false) }
                sendUiEvent(AdminUsuariosUiEvent.ShowSuccess(successMsg))
                loadUsuarios()
            }

            is Resource.Error -> {
                reduce { it.copy(isLoading = false) }
                sendUiEvent(AdminUsuariosUiEvent.ShowError(result.message ?: errorMsg))
            }
        }
    }

    private fun cambiarRol(id: Int, rol: String) {
        viewModelScope.launch {
            cambiarRolUsuarioUseCase(id, rol).collect { result ->
                handleMutation(
                    result,
                    "Rol actualizado exitosamente",
                    "Error al cambiar rol"
                )
            }
        }
    }

    private fun toggleEstado(id: Int, activo: Boolean) {
        val msg = if (activo) "Usuario activado" else "Usuario desactivado"

        viewModelScope.launch {
            cambiarEstadoUsuarioUseCase(id, activo).collect { result ->
                handleMutation(
                    result,
                    msg,
                    "Error al cambiar estado"
                )
            }
        }
    }

    private fun confirmDelete() {
        val usuario = _state.value.usuarioSeleccionado ?: return

        viewModelScope.launch {
            deleteUsuarioUseCase(usuario.usuarioId).collect { result ->
                handleMutation(
                    result,
                    "Usuario eliminado exitosamente",
                    "Error al eliminar usuario"
                )
            }
        }
    }
}
