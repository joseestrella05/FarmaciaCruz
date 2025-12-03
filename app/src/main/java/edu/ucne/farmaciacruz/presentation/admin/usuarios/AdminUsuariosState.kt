package edu.ucne.farmaciacruz.presentation.admin.usuarios

import edu.ucne.farmaciacruz.domain.model.UsuarioAdmin

data class AdminUsuariosState(
    val isLoading: Boolean = false,
    val usuarios: List<UsuarioAdmin> = emptyList(),
    val usuariosFiltrados: List<UsuarioAdmin> = emptyList(),
    val searchQuery: String = "",
    val selectedRol: String? = null,
    val selectedEstado: Boolean? = null,
    val roles: List<String> = listOf("Cliente", "Administrador"),
    val usuarioSeleccionado: UsuarioAdmin? = null,
    val showDeleteDialog: Boolean = false,
    val showEditRolDialog: Boolean = false,
    val showToggleEstadoDialog: Boolean = false,
    val error: String? = null
)