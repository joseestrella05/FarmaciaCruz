package edu.ucne.farmaciacruz.domain.usecase.admin

import edu.ucne.farmaciacruz.data.repository.AdminRepository
import edu.ucne.farmaciacruz.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CambiarRolUsuarioUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(usuarioId: Int, nuevoRol: String): Flow<Resource<Unit>> {
        return adminRepository.cambiarRolUsuario(usuarioId, nuevoRol)
    }
}