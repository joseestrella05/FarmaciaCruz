package edu.ucne.farmaciacruz.domain.usecase.admin

import edu.ucne.farmaciacruz.data.repository.AdminRepository
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.model.UsuarioAdmin
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllUsuariosUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(): Flow<Resource<List<UsuarioAdmin>>> {
        return adminRepository.getAllUsuarios()
    }
}