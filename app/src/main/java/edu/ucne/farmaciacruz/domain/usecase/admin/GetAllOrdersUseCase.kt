package edu.ucne.farmaciacruz.domain.usecase.admin

import edu.ucne.farmaciacruz.data.repository.AdminRepository
import edu.ucne.farmaciacruz.domain.model.OrderAdmin
import edu.ucne.farmaciacruz.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllOrdersUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(): Flow<Resource<List<OrderAdmin>>> {
        return adminRepository.getAllOrders()
    }
}