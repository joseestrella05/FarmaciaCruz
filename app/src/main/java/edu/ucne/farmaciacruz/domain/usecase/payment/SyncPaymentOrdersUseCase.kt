package edu.ucne.farmaciacruz.domain.usecase.payment

import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SyncPaymentOrdersUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    suspend operator fun invoke(): Flow<Resource<Unit>> {
        return repository.syncOrders()
    }
}