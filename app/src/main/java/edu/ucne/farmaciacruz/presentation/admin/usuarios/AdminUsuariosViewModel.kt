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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
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

    fun onEvent(event: AdminUsuariosUiEvent) {
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

            is AdminUsuariosUiEvent.ShowError -> {
                viewModelScope.launch {
                    sendUiEvent(event)
                }
            }

            is AdminUsuariosUiEvent.ShowSuccess -> {
                viewModelScope.launch {
                    sendUiEvent(event)
                }
            }
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
            getAllUsuariosUseCase().collect { r ->
                when (r) {
                    is Resource.Loading ->
                        reduce { it.copy(isLoading = true) }

                    is Resource.Success -> {
                        reduce {
                            it.copy(
                                isLoading = false,
                                usuarios = r.data.orEmpty()
                            )
                        }
                        applyFilters()
                    }

                    is Resource.Error -> {
                        reduce { it.copy(isLoading = false) }
                        sendUiEvent(AdminUsuariosUiEvent.ShowError(r.message ?: "Error"))
                    }
                }
            }
        }
    }

    private fun applyFilters() {
        val stateValue = _state.value
        val filtered = stateValue.usuarios.filter { usuario ->

            val matchesSearch =
                stateValue.searchQuery.isBlank() ||
                        usuario.nombreCompleto.contains(stateValue.searchQuery, ignoreCase = true) ||
                        usuario.email.contains(stateValue.searchQuery, ignoreCase = true)

            val matchesRol =
                stateValue.selectedRol == null ||
                        usuario.rol == stateValue.selectedRol

            val matchesEstado =
                stateValue.selectedEstado == null ||
                        usuario.activo == stateValue.selectedEstado

            matchesSearch && matchesRol && matchesEstado
        }

        reduce { it.copy(usuariosFiltrados = filtered) }
    }

    private fun cambiarRol(id: Int, rol: String) {
        viewModelScope.launch {
            cambiarRolUsuarioUseCase(id, rol).collect { r ->
                when (r) {
                    is Resource.Loading ->
                        reduce { it.copy(isLoading = true) }

                    is Resource.Success -> {
                        reduce { it.copy(isLoading = false) }
                        sendUiEvent(AdminUsuariosUiEvent.ShowSuccess("Rol actualizado"))
                        onEvent(AdminUsuariosEvent.DismissDialogs)
                        loadUsuarios()
                    }

                    is Resource.Error -> {
                        reduce { it.copy(isLoading = false) }
                        sendUiEvent(AdminUsuariosUiEvent.ShowError(r.message ?: "Error"))
                    }
                }
            }
        }
    }

    private fun toggleEstado(id: Int, activo: Boolean) {
        viewModelScope.launch {
            cambiarEstadoUsuarioUseCase(id, activo).collect { r ->
                when (r) {
                    is Resource.Loading ->
                        reduce { it.copy(isLoading = true) }

                    is Resource.Success -> {
                        reduce { it.copy(isLoading = false) }
                        sendUiEvent(AdminUsuariosUiEvent.ShowSuccess("Estado actualizado"))
                        onEvent(AdminUsuariosEvent.DismissDialogs)
                        loadUsuarios()
                    }

                    is Resource.Error -> {
                        reduce { it.copy(isLoading = false) }
                        sendUiEvent(AdminUsuariosUiEvent.ShowError(r.message ?: "Error"))
                    }
                }
            }
        }
    }

    private fun confirmDelete() {
        val usuario = _state.value.usuarioSeleccionado ?: return

        viewModelScope.launch {
            deleteUsuarioUseCase(usuario.usuarioId).collect { r ->
                when (r) {
                    is Resource.Loading ->
                        reduce { it.copy(isLoading = true) }

                    is Resource.Success -> {
                        reduce { it.copy(isLoading = false) }
                        sendUiEvent(AdminUsuariosUiEvent.ShowSuccess("Usuario eliminado"))
                        onEvent(AdminUsuariosEvent.DismissDialogs)
                        loadUsuarios()
                    }

                    is Resource.Error -> {
                        reduce { it.copy(isLoading = false) }
                        sendUiEvent(AdminUsuariosUiEvent.ShowError(r.message ?: "Error"))
                    }
                }
            }
        }
    }
}