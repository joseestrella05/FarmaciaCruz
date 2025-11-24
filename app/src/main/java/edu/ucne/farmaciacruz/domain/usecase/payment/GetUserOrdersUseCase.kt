package edu.ucne.farmaciacruz.domain.usecase.payment

import edu.ucne.farmaciacruz.domain.model.PaymentOrder
import edu.ucne.farmaciacruz.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserOrdersUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    operator fun invoke(usuarioId: Int): Flow<List<PaymentOrder>> {
        return repository.getOrdersByUser(usuarioId)
    }
}