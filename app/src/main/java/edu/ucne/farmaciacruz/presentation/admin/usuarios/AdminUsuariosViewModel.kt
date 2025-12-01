package edu.ucne.farmaciacruz.presentation.admin.usuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.model.UsuarioAdmin
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

    private val _effect = Channel<AdminUsuariosEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadUsuarios()
    }

    fun onEvent(event: AdminUsuariosEvent) {
        when (event) {
            AdminUsuariosEvent.LoadUsuarios -> loadUsuarios()
            is AdminUsuariosEvent.SearchQueryChanged -> search(event.query)
            is AdminUsuariosEvent.RolFilterSelected -> filterByRol(event.rol)
            is AdminUsuariosEvent.EstadoFilterSelected -> filterByEstado(event.activo)
            is AdminUsuariosEvent.UsuarioSelected -> selectUsuario(event.usuario)
            AdminUsuariosEvent.DismissDialogs -> dismissDialogs()
            is AdminUsuariosEvent.ShowCambiarRolDialog -> showCambiarRolDialog(event.usuario)
            is AdminUsuariosEvent.CambiarRol -> cambiarRol(event.usuarioId, event.nuevoRol)
            is AdminUsuariosEvent.ShowToggleEstadoDialog -> showToggleEstadoDialog(event.usuario)
            is AdminUsuariosEvent.ToggleEstado -> toggleEstado(event.usuarioId, event.activo)
            is AdminUsuariosEvent.ShowDeleteDialog -> showDeleteDialog(event.usuario)
            AdminUsuariosEvent.ConfirmDelete -> confirmDelete()
            AdminUsuariosEvent.Refresh -> loadUsuarios()
        }
    }

    private fun loadUsuarios() {
        viewModelScope.launch {
            getAllUsuariosUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        val usuarios = result.data ?: emptyList()
                        _state.update {
                            it.copy(
                                isLoading = false,
                                usuarios = usuarios,
                                usuariosFiltrados = usuarios,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        _effect.send(
                            AdminUsuariosEffect.ShowError(
                                result.message ?: "Error al cargar usuarios"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun search(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    private fun filterByRol(rol: String?) {
        _state.update { it.copy(selectedRol = rol) }
        applyFilters()
    }

    private fun filterByEstado(activo: Boolean?) {
        _state.update { it.copy(selectedEstado = activo) }
        applyFilters()
    }

    private fun applyFilters() {
        val current = _state.value
        val filtered = current.usuarios.filter { usuario ->
            val matchesSearch = current.searchQuery.isBlank() ||
                    usuario.nombreCompleto.contains(current.searchQuery, ignoreCase = true) ||
                    usuario.email.contains(current.searchQuery, ignoreCase = true)

            val matchesRol = current.selectedRol == null ||
                    usuario.rol == current.selectedRol

            val matchesEstado = current.selectedEstado == null ||
                    usuario.activo == current.selectedEstado

            matchesSearch && matchesRol && matchesEstado
        }

        _state.update { it.copy(usuariosFiltrados = filtered) }
    }

    private fun selectUsuario(usuario: UsuarioAdmin) {
        _state.update { it.copy(usuarioSeleccionado = usuario) }
    }

    private fun showCambiarRolDialog(usuario: UsuarioAdmin) {
        _state.update {
            it.copy(
                usuarioSeleccionado = usuario,
                showEditRolDialog = true
            )
        }
    }

    private fun showToggleEstadoDialog(usuario: UsuarioAdmin) {
        _state.update {
            it.copy(
                usuarioSeleccionado = usuario,
                showToggleEstadoDialog = true
            )
        }
    }

    private fun showDeleteDialog(usuario: UsuarioAdmin) {
        _state.update {
            it.copy(
                usuarioSeleccionado = usuario,
                showDeleteDialog = true
            )
        }
    }

    private fun dismissDialogs() {
        _state.update {
            it.copy(
                showEditRolDialog = false,
                showToggleEstadoDialog = false,
                showDeleteDialog = false,
                usuarioSeleccionado = null
            )
        }
    }

    private fun cambiarRol(usuarioId: Int, nuevoRol: String) {
        viewModelScope.launch {
            cambiarRolUsuarioUseCase(usuarioId, nuevoRol).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        _effect.send(
                            AdminUsuariosEffect.ShowSuccess("Rol actualizado exitosamente")
                        )
                        dismissDialogs()
                        loadUsuarios()
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false) }
                        _effect.send(
                            AdminUsuariosEffect.ShowError(
                                result.message ?: "Error al cambiar rol"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun toggleEstado(usuarioId: Int, nuevoEstado: Boolean) {
        viewModelScope.launch {
            cambiarEstadoUsuarioUseCase(usuarioId, nuevoEstado).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        val mensaje = if (nuevoEstado) "Usuario activado" else "Usuario desactivado"
                        _effect.send(AdminUsuariosEffect.ShowSuccess(mensaje))
                        dismissDialogs()
                        loadUsuarios()
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false) }
                        _effect.send(
                            AdminUsuariosEffect.ShowError(
                                result.message ?: "Error al cambiar estado"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun confirmDelete() {
        val usuario = _state.value.usuarioSeleccionado ?: return

        viewModelScope.launch {
            deleteUsuarioUseCase(usuario.usuarioId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        _effect.send(
                            AdminUsuariosEffect.ShowSuccess("Usuario eliminado exitosamente")
                        )
                        dismissDialogs()
                        loadUsuarios()
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false) }
                        _effect.send(
                            AdminUsuariosEffect.ShowError(
                                result.message ?: "Error al eliminar usuario"
                            )
                        )
                    }
                }
            }
        }
    }
}