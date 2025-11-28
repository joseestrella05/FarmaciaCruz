package edu.ucne.farmaciacruz.domain.usecase.order

import edu.ucne.farmaciacruz.domain.model.Order
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserOrdersUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    operator fun invoke(usuarioId: Int): Flow<Resource<List<Order>>> {
        return repository.getUserOrders(usuarioId)
    }
}